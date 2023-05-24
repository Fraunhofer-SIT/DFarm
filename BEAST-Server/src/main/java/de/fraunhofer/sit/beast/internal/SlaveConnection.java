package de.fraunhofer.sit.beast.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.util.Enumeration;

import org.apache.commons.io.IOUtils;

import de.fraunhofer.sit.beast.applications.Main;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SlaveConnection {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SlaveConnection other = (SlaveConnection) obj;
		if (baseUrl == null) {
			if (other.baseUrl != null)
				return false;
		} else if (!baseUrl.equals(other.baseUrl))
			return false;
		return true;
	}

	/**
	 * Some arbitrary buffer size.
	 */
	private static final int BUFFER_SIZE = 4096;
	private URL baseUrl;
	private int startRange = -1;
	private int endRange = -1;
	private long lastHeartbeat = System.currentTimeMillis();
		

	public SlaveConnection(URL url, int startRange, int endRange) throws IOException {
		this.baseUrl = url;
		this.startRange = startRange;
		this.endRange = endRange;
	}

	public URL getBaseUrl() {
		return baseUrl;
	}

	private String getStringResponse(String file) throws IOException {
		URL u = getURL(file);
		URLConnection c = u.openConnection();
		HttpURLConnection http = ((HttpURLConnection) c);
		http.setDoOutput(true);
		try (InputStream inp = c.getInputStream()) {
			String res = IOUtils.toString(inp);
			if (http.getResponseCode() != 200)
				throw new IOException(res);
			return res;
		}
	}

	public URL getURL(String file) throws MalformedURLException {
		return new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), file);
	}

	public boolean isInRange(int devid) {
		return devid >= startRange && devid <= endRange;
	}

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	/**
	 * Forwards a request to an URL specified in target. This is used to forward
	 * messages to the slave servers.
	 * 
	 * @param target   the target of the request
	 * @param request  the source request
	 * @param output   the output stream the redirected output should be written
	 *                 to.
	 * @param response The response object, can be null. If not null, some
	 *                 additional headers such as content types and length are
	 *                 set.
	 * @return the url connection used to forward the request
	 * @throws IOException
	 */
	public HttpURLConnection forward(String target, HttpServletRequest request, OutputStream output,
			HttpServletResponse response) throws IOException {
		String s = request.getQueryString() != null ? "?" + request.getQueryString() : "";
		URL u = getURL(target + s);
		HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
		urlConnection.setUseCaches(false);
		urlConnection.setRequestMethod(request.getMethod());
		long l = -1;
		Enumeration<String> it = request.getHeaderNames();
		while (it.hasMoreElements()) {
			String e = it.nextElement();

			final Enumeration<String> values = request.getHeaders(e);
			while (values.hasMoreElements()) {
				final String value = values.nextElement();
				if (e.equals("Content-Length"))
					l = Long.parseLong(value);
				urlConnection.addRequestProperty(e, value);
			}
		}
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		try {
			urlConnection.connect();
		} catch (Exception e) {
			throw new RuntimeException("Could not connect to " + u, e);
		}
		byte[] buf = new byte[BUFFER_SIZE];
		if (!urlConnection.getRequestMethod().equals("GET")) {
			OutputStream os = urlConnection.getOutputStream();
			IOUtils.copyLarge(request.getInputStream(), os, 0, l, buf);
			os.close();
		}
		if (urlConnection.getResponseCode() == 200) {
			if (response != null) {
				response.setContentType(urlConnection.getContentType());
				response.setContentLengthLong(urlConnection.getContentLengthLong());
			}
			// if a logcat request is forwarded, make sure to regularly flush
			if ("Yes".equals(urlConnection.getHeaderField("Stream"))) {
				BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				
				// this will run until the client stops listening.
				// when that happens, jetty sends a 500 response
				// the response will never be read anyway
				while (true) {
					output.write(br.readLine().getBytes());
					output.write('\n');
					output.flush();
				}
			} else {
				IOUtils.copyLarge(urlConnection.getInputStream(), output,buf);
			}
		}
		else {
			InputStream error = urlConnection.getErrorStream();
			if (error != null)
				IOUtils.copyLarge(error, output, buf);
		}
		return urlConnection;
	}

	public byte[] forward(String target, HttpServletRequest request) throws IOException, HttpCodeException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpURLConnection c = forward(target, request, output, null);
		if (c.getResponseCode() != 200 && c.getResponseCode() != 204)
			throw new HttpCodeException(c.getResponseCode(), new String(output.toByteArray(), "UTF-8"));
		return output.toByteArray();
	}
}
