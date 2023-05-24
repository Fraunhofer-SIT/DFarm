package de.fraunhofer.sit.beast.applications;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.listener.ELContextCleaner;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.jboss.resteasy.resteasy_jaxrs.i18n.LogMessages;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.ExceptionProvider;
import de.fraunhofer.sit.beast.internal.Config;
import de.fraunhofer.sit.beast.internal.ConfigBase;
import de.fraunhofer.sit.beast.internal.DeviceManager;
import de.fraunhofer.sit.beast.internal.HttpCodeException;
import de.fraunhofer.sit.beast.internal.LogUtils;
import de.fraunhofer.sit.beast.internal.SlaveConnection;
import de.fraunhofer.sit.beast.internal.android.AndroidFileListing;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.utils.IOUtils;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class Main {
	public static final String API_RANGES_END = "/api/ranges/end";
	public static final String API_RANGES_START = "/api/ranges/start";
	private static final String heartbeatTarget = "/heartbeat";
	private static final String certTarget = "/createCert";
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	public static boolean commanderMode = true;
	private static ConcurrentHashMap<SlaveConnection, SlaveConnection> slaves = new ConcurrentHashMap<SlaveConnection, SlaveConnection>();

	private static int port = 5080;


	private static void maintainSlaves() {
		for (Map.Entry<SlaveConnection, SlaveConnection> entry : slaves.entrySet()) {
			SlaveConnection slave = entry.getValue();
			String HeartbeatTimeout = ConfigBase.getProperties().get("HeartbeatTimeout");
			if (HeartbeatTimeout == null)
				HeartbeatTimeout = "60000";
			if ((System.currentTimeMillis() - slave.getLastHeartbeat()) > Integer.valueOf(HeartbeatTimeout)) {
				slaves.remove(slave);
			} else {
				continue;
			}

		}
	}

	public static void main(String[] args) throws Exception {
		
		LogUtils.initialize();
		
		for (int i = 0; i < args.length; i++) { 
			final String arg = args[i];
			if (arg.equals("--port")) {
				port = Integer.valueOf(args[i + 1]);
				i++;
				continue;
			}

			if (arg.contentEquals("-config")) {
				LOGGER.warn("Loading external Config file");
				String path = args[i + 1];
				try {
					ConfigBase.loadFile(path);
				} catch (Exception e) {
					LOGGER.error(e);
				}
				i++;
				continue;

			}

			if (arg.contentEquals("-slave")) {
				final String masterServerIp = ConfigBase.getProperties().get("MasterServer.IP");
				final String masterServerPort = ConfigBase.getProperties().get("MasterServer.Port");
				final int startRange = Config.getDeviceStartRange();
				final int endRange = Config.getDeviceEndRange();
				URL dest = new URL("http", masterServerIp, Integer.valueOf(masterServerPort),
						heartbeatTarget + "?port=" + Main.port + "&startRange=" + startRange + "&endRange=" + endRange);
				String slaveIp = InetAddress.getLocalHost().getHostAddress();
				commanderMode = false;
				Thread thrConnectToMain = new Thread() {
					@Override
					public void run() {
						try {
							while (true) {
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e1) {
								}
								try {
									HttpURLConnection con = (HttpURLConnection) dest.openConnection();
									con.setRequestMethod("GET");
									if (con.getResponseCode() != 200) {
										LOGGER.error(String.format(
												"Connection to Master failed for Slave: %s failed with response code %d",
												slaveIp, con.getResponseCode()));
									}
								} catch (Throwable e) {
									LOGGER.error("Error while connecting to Master: " + dest.toString(), e);
								}
	
							}
						} finally {
							LOGGER.error("Connection to main server thread is in finally block. This should not happen!");
							
						}
					}
				};
				thrConnectToMain.start();
				i++;
				continue;

			}
		}
		try {
			Database.initialize();
		} catch (Throwable t) {
			LOGGER.error("An error occurred while initializing the DB", t);
			System.exit(20);
			return;
		}
		if (commanderMode) {
			Thread slaveMaintainer = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						maintainSlaves();
						try {
							Thread.sleep(500);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			slaveMaintainer.start();
		}

		DeviceManager.DEVICE_MANAGER.initialize();
		LOGGER.warn("Starting the Best Environment for Analyzing Smartphoneapplications and Testing");
		WebAppContext wacontext = new WebAppContext();
		ELContextCleaner.class.getName();
		System.setProperty("jakarta.ws.rs.Application", ApplicationConfig.class.getCanonicalName());
		System.setProperty("resteasy.providers", ExceptionProvider.class.getName());
		wacontext.setParentLoaderPriority(true);
		wacontext.setThrowUnavailableOnStartupException(true);
		wacontext.setConfigurations(new Configuration[] { new WebXmlConfiguration() {

		    protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException
		    {
		        Resource dir = context.getBaseResource();
		        if (dir != null && dir.isDirectory())
		        {
		            // do web.xml file
		            Resource web = dir.addPath("web.xml");
		            if (web.exists())
		                return web;
		            
		            Resource cp = Resource.newClassPathResource("/web.xml");
		            if (cp != null && cp.exists())
		            	return cp;
		            cp = Resource.newClassPathResource("web.xml");
		            if (cp != null && cp.exists())
		            	return cp;
		        }
		        return super.findWebXml(wacontext);
		    }
		   }
		});
		/*wacontext.setParentLoaderPriority(true);
		wacontext.setContextPath("/");

		wacontext.setResourceBase("/");
		wacontext.setParentLoaderPriority(true);*/
		wacontext.setResourceBase(new File("").getAbsolutePath());

		QueuedThreadPool threadPool = new QueuedThreadPool(100000, 10);
		Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        wacontext.setServer(server);
		wacontext.setClassLoader(new ClassLoader(Thread.currentThread().getContextClassLoader()) {
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				return super.findClass(name);
			}
		});
		server.setHandler(new AbstractHandler() {
			final Pattern extractDevId = Pattern.compile("/api/devices/(\\d+).*");

			@Override
			protected void doStart() throws Exception {
				Class.forName("org.eclipse.jetty.servlet.listener.ELContextCleaner");
				//super.start();
				wacontext.start();
			}

			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				if (target.equals("/api") || target.equals("/api/")) {
					response.sendRedirect("/api-docs/");
					return;
				}
				MainUtils.setPublicHostname(baseRequest.getLocalAddr());
				if (target.startsWith(certTarget) && (commanderMode)) {
					response.setStatus(200);
					response.setContentType("text/html");
					int devId = Integer.parseInt(baseRequest.getOriginalURI().split("=")[1]);
					
					File tmp = File.createTempFile("openvpn-client-gen", ".sh");
					File certFile = File.createTempFile("openvpn-cert", ".cer");
					tmp.setExecutable(true);
					
					try (InputStream inp = IOUtils.getResource("/res/scripts/openvpn-client-gen.sh")) {
						org.apache.commons.io.IOUtils.copy(inp, new FileOutputStream(tmp));
					}
					//start the ovpn client creation script
					ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "cd /home/controller/Repositories/d-farm/BEAST-Server/res/scripts/ && sudo ./openvpn-client-gen.sh " + devId);
					Process p = pb.start();
					try (InputStream inp = IOUtils.getResource(MessageFormat.format("/res/certs/{0}.ovpn", devId))) {
						org.apache.commons.io.IOUtils.copy(inp, new FileOutputStream(certFile));
					}
					while (p.isAlive()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							LOGGER.error("error during client-cert creation", e);
						}
					}
					if (p.exitValue() != 0) {
						response.getOutputStream().write(org.apache.commons.io.IOUtils.toString(p.getErrorStream()).getBytes("utf-8"));
						response.getOutputStream().close();
						return;
					}
					
					//copy the client script to the device
					try (FileInputStream inp = new FileInputStream(certFile)) {
						//response.getOutputStream().write("test".getBytes());
						response.getOutputStream().write(org.apache.commons.io.IOUtils.toByteArray(inp));
					} finally {
						response.getOutputStream().close();
					}
					
					response.getOutputStream().close();
					return;
				}
				
				if (target.equals(heartbeatTarget)) {
					response.setStatus(200);
					response.setContentType("text/html");
					String slaveURLString = baseRequest.getRemoteInetSocketAddress().getHostString();
					String port = request.getParameter("port");
					int startRange = Integer.parseInt(request.getParameter("startRange"));
					int endRange = Integer.parseInt(request.getParameter("endRange"));
					LOGGER.info(String.format("Slave %s:%s is alive", slaveURLString, port));
					URL slaveURL = new URL("http://" + slaveURLString + ":" + port);
					SlaveConnection connection;
					try {
						connection = new SlaveConnection(slaveURL,startRange,endRange);
					} catch (IOException e) {
						LOGGER.error(String.format("Could not connect to %s", slaveURL), e);
						response.getOutputStream().close();
						return;
					}
					if (!slaves.contains(connection) && connection != null) {
						slaves.put(connection, connection);

					} else {
						connection = slaves.get(connection);
						connection.setLastHeartbeat(System.currentTimeMillis());
					}
					response.getOutputStream().write(slaveURL.toString().getBytes("UTF-8"));
					response.getOutputStream().close();
					return;

				}

				if (target.startsWith("/api/") && !target.equals("/api/openapi.json")) {
					if (commanderMode) {
						// We have to forward some requests and aggregate
						// others.
						Matcher matcher = extractDevId.matcher(target);
						if (matcher.matches()) {
							final int devid = Integer.parseInt(matcher.group(1));
							for (Map.Entry<SlaveConnection, SlaveConnection> entry : slaves.entrySet()) {
								SlaveConnection slave = entry.getValue();
								if (slave.isInRange(devid)) {
									LOGGER.error("Forwarding " + request.getQueryString() + " to slave" + slave.getBaseUrl());
									slave.forward(target, request, response.getOutputStream(), response);
									response.getOutputStream().close();
									return;
								}
							}
							response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(new APIException(404, "Device not found at any slave")));
							response.getOutputStream().close();
						} else {
							String normalized = target;
							if (target.endsWith("/")) {
								normalized = target.substring(0, target.length() - 1);
							}
							switch (normalized) {
							case "/api/devices":
								handleDeviceListing(target, request, response);
								break;
							case "/api/devices/releaseAll":
								handleReleaseAll(target, request, response);
								break;
							case "/api/devices/reserve":
								handleReserve(target, request, response);
								break;
							default:
								response.setStatus(500);
								response.getOutputStream().write(
										new String("<h2>500 - not implemented: " + target + "</h2>").getBytes("UTF-8"));
								response.getOutputStream().close();
							}
						}

					} else {
						wacontext.handle(target, baseRequest, request, response);
					}
					return;
				}
				if (target.startsWith("/api/openapi."))
				{
					Reader readerFinal = new Reader(new OpenAPI());
					OpenAPI o = readerFinal.read(new ApplicationConfig().getMyClasses());
					try {
						String s = Json.pretty().writeValueAsString(o);
						try (PrintWriter w = response.getWriter()) {
							w.print(s);
						}
						return;
					} catch (JsonProcessingException e) {
						LOGGER.error("Could not convert to JSON", e);					}
				}
				if (target.equals("/api-docs") || target.equals("/api-docs/"))
					target = "/api-docs/index.html";

				try {
					String path = target;
					path = "/res/content" + path;
					try (InputStream inp = IOUtils.getResource(path)) {
						if (inp != null) {
							String lpath = path.toLowerCase();
							String contentType = null;
							if (lpath.endsWith(".html"))
								contentType = "text/html";
							else if (lpath.endsWith(".css"))
								contentType = "text/css";
							else if (lpath.endsWith(".gif"))
								contentType = "image/gif";
							else if (lpath.endsWith(".png"))
								contentType = "image/png";
							else if (lpath.endsWith(".json"))
								contentType = "application/json";
							if (contentType != null)
								response.setContentType(contentType);

							org.apache.commons.io.IOUtils.copy(inp, response.getOutputStream());
						} else {
							response.setStatus(404);
							response.getOutputStream()
									.write(new String("<h2>404 - File not found</h2>").getBytes("UTF-8"));
						}
					}
				} finally {
					// This is very important...
					response.getOutputStream().close();
					response.flushBuffer();
				}
			}

			private void handleReserve(String target, HttpServletRequest request, HttpServletResponse response)
					throws IOException, UnsupportedEncodingException {
				response.setContentType("application/json");
				Exception ex = null;
				if (!request.getMethod().equals("GET")) {
					byte[] b = org.apache.commons.io.IOUtils.toByteArray(request.getInputStream());
					final HttpServletRequest origRequest =request;
					request = new HttpServletRequestWrapper(origRequest) {
						public ServletInputStream getInputStream() {
							ByteArrayInputStream bis = new ByteArrayInputStream(b);
							return new ServletInputStream() {

								@Override
								public boolean isFinished() {
									return true;
								}

								@Override
								public boolean isReady() {
									return true;
								}

								@Override
								public void setReadListener(ReadListener readListener) {
									throw new RuntimeException();
								}

								@Override
								public int read() throws IOException {
									return bis.read();
								}

								
							};
						}
					};
				}
				
				List<SlaveConnection> res = new ArrayList<>(slaves.keySet());
				Collections.shuffle(res);
				for (SlaveConnection slave : res) {
					try {
						byte[] cres = slave.forward(target, request);
						response.setContentLength(cres.length);
						response.getOutputStream().write(cres);
						response.getOutputStream().close();
						return;
					} catch (Exception e) {
						ex = e;
						LOGGER.error("Error on slave" +slave.getBaseUrl(), e);
					}
				}
				if (ex != null) {
					if (ex instanceof HttpCodeException) {
						HttpCodeException e = (HttpCodeException) ex;
						response.setStatus(e.responseCode);
					} else
						response.setStatus(500);
					response.getOutputStream().write(ex.getMessage().getBytes("UTF-8"));
				}
				response.getOutputStream().close();
			}

			private void handleReleaseAll(String target, HttpServletRequest request, HttpServletResponse response)
					throws IOException, UnsupportedEncodingException {
				// Forward to all
				for (Map.Entry<SlaveConnection, SlaveConnection> entry : slaves.entrySet()) {
					SlaveConnection slave = entry.getValue();
					{
						try {
							slave.forward(target, request);
						} catch (HttpCodeException e) {
							response.setStatus(e.responseCode);
							response.getOutputStream().write(e.getMessage().getBytes("UTF-8"));
						}
					}
					response.getOutputStream().close();
				}
			}

			public void handleDeviceListing(String target, HttpServletRequest request, HttpServletResponse response)
					throws IOException, JsonParseException, JsonMappingException, UnsupportedEncodingException,
					JsonProcessingException {
				response.setContentType("application/json");
				final ObjectMapper mapper = new ObjectMapper();
				final List<DeviceInformation> allDevs = new ArrayList<>();
				for (Map.Entry<SlaveConnection, SlaveConnection> entry : slaves.entrySet()) {
					SlaveConnection slave = entry.getValue();
					{
						try {
							byte[] res = slave.forward(target, request);
							DeviceInformation[] r = mapper.readValue(res, DeviceInformation[].class);
							if (r != null) {
								for (DeviceInformation i : r) {
									i.managerHostname = slave.getBaseUrl().getHost();
									allDevs.add(i);
								}
									
							}
						} catch (HttpCodeException e) {
							response.setStatus(e.responseCode);
							response.getOutputStream().write(e.getMessage().getBytes("UTF-8"));
						}
					}
				}

				response.getOutputStream().write(mapper.writeValueAsBytes(allDevs));
				response.getOutputStream().close();
			}
			

		});
		System.setProperty("org.jboss.logging.provider", "log4j2");
		server.start();

	}

	
}
