package de.fraunhofer.sit.beast.internal.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.android.ddmlib.IDevice;

import de.fraunhofer.sit.beast.api.data.network.ports.ForwardingDirection;
import de.fraunhofer.sit.beast.api.data.network.ports.PortForwarding;
import de.fraunhofer.sit.beast.api.data.network.ports.Protocol;
import de.fraunhofer.sit.beast.internal.interfaces.IPortForwardingListing;
import de.fraunhofer.sit.beast.internal.persistance.Database;

public class AndroidPortForwardingListing implements IPortForwardingListing {

	private static final Logger LOGGER = LogManager.getLogger(AndroidPortForwardingListing.class);

	/**
	 * This class is responsible to forward exactly one port.
	 */
	static class Redirector {
		private boolean abort;
		private ServerSocket socket;
		private int localPort;

		public Redirector(int localPort) {
			this.localPort = localPort;
		}

		public void stop() {
			abort = true;
			IOUtils.closeQuietly(socket);
		}

		public void start() throws IOException {
			socket = new ServerSocket(0);

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					while (!abort) {
						try {
							Socket accepted = socket.accept();
							Socket fwd = new Socket(InetAddress.getLoopbackAddress(), localPort);
							bind(accepted, fwd);
							bind(fwd, accepted);
						} catch (IOException e) {
							LOGGER.warn(
									String.format("An error occurred while forwarding from %d", socket.getLocalPort()),
									e);
						}

					}
				}

				private void bind(Socket input, Socket output) {
					Thread thrRead = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								IOUtils.copy(input.getInputStream(), output.getOutputStream());
							} catch (IOException e) {
								LOGGER.warn(String.format("An error occurred while forwarding"), e);
							}
						}
					});
					thrRead.setDaemon(true);
					thrRead.setName(String.format("Proxying %d", localPort));
					thrRead.start();
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}

	private AndroidDevice device;

	public AndroidPortForwardingListing(AndroidDevice device) {
		this.device = device;
	}

	@Override
	public Collection<PortForwarding> listPortForwardings() {
		if (AndroidDeviceManager.MANAGER == null)
			//Still initializing
			return Collections.emptyList();
		String output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "forward", "--list");
		List<PortForwarding> list = new ArrayList<>();
		add(ForwardingDirection.DEVICE_IS_SERVER, device.getAndroidDevice(), list, output);
		output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "reverse", "--list");
		add(ForwardingDirection.DEVICE_IS_CLIENT, device.getAndroidDevice(), list, output);
		return list;
	}

	private void add(ForwardingDirection direction, IDevice iDevice, List<PortForwarding> list, String output) {
		try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
			while (true) {
				// adb forward --list displays ALL devices and forward port
				// forwardings as per documentation
				// Sample line for adb forward --list
				// W56AX048B08D0003193 tcp:2000 tcp:4000

				// adb reverse --list displays only reverse port forwardings of
				// one device as per documentation
				// Sample line for adb reverse --list
				// (reverse) tcp:2000 tcp:4000
				String line = reader.readLine();
				if (line == null || line.isEmpty())
					break;
				int fst = line.indexOf(' ');
				String dev = line.substring(0, fst);
				String part2 = line.substring(fst + 1);
				int x = part2.indexOf(" ");
				String part3 = part2.substring(x + 1);
				part2 = part2.substring(0, x);

				PortForwarding pfwd = new PortForwarding();
				String devicePortion, hostPortion;
				if (direction == ForwardingDirection.DEVICE_IS_SERVER) {
					if (!dev.equals(iDevice.getSerialNumber()))
						continue;
					hostPortion = part2;
					devicePortion = part3;
				} else {
					hostPortion = part3;
					devicePortion = part2;
				}

				if (!hostPortion.startsWith("tcp:"))
					// different protocol
					continue;
				pfwd.direction = direction;
				pfwd.portOnHostMachine = Integer.parseInt(hostPortion.substring(4));

				int p = devicePortion.indexOf(':');
				String deviceProtocol = devicePortion.substring(0, p);
				switch (deviceProtocol) {
				case "tcp":
					pfwd.protocolOnDevice = Protocol.TCP;
					break;
				case "jdwp":
					pfwd.protocolOnDevice = Protocol.JDWP;
					break;
				default:
					// different protocol
					continue;
				}
				pfwd.portOnDevice = Integer.parseInt(devicePortion.substring(p + 1));
				pfwd.id = pfwd.portOnDevice;
				list.add(pfwd);

			}
		} catch (IOException e) {
			throw new RuntimeException(output);
		}
	}

	@Override
	public void removePortForwarding(PortForwarding forwarding) {
		try {
			String output;
			if (forwarding.direction == ForwardingDirection.DEVICE_IS_SERVER)
				output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "forward", "--remove",
						"tcp:" + String.valueOf(forwarding.portOnHostMachine));
			else
				output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "reverse", "--remove",
						getDevicePortion(forwarding));
			if (!output.isEmpty())
				throw new RuntimeException(output);
		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	@Override
	public synchronized void createPortForwarding(PortForwarding forwarding) {
		try {
			int portOnHost;
			try (ServerSocket s = new ServerSocket(0)) {
				// Best effort
				portOnHost = s.getLocalPort();
			}
			forwarding.portOnHostMachine = portOnHost;
			String local = "tcp:" + portOnHost;
			String remote = getDevicePortion(forwarding);

			String output;
			if (forwarding.direction == ForwardingDirection.DEVICE_IS_SERVER)
				// forward
				output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "forward", "--no-rebind",
						local, remote);
			else
				// reverse
				output = runADBCommand("-s", device.getAndroidDevice().getSerialNumber(), "reverse", "--no-rebind",
						remote, local);

			if (!output.isEmpty())
				throw new RuntimeException(output);

		} catch (Exception e) {
			throw AndroidUtils.translateAndroidException(e);
		}
	}

	private String getDevicePortion(PortForwarding forwarding) {
		String remote;
		switch (forwarding.protocolOnDevice) {
		case TCP:
			remote = "tcp:" + forwarding.portOnDevice;
			break;
		case JDWP:
			remote = "jdwp:" + forwarding.portOnDevice;
			break;
		default:
			throw new RuntimeException(
					String.format("Protocol on device unsupported: %s", forwarding.protocolOnDevice));
		}
		return remote;
	}

	private String runADBCommand(String... command) {
		try {
			File adb = AndroidDeviceManager.MANAGER.getADBLocation();
			List<String> cmd = new ArrayList<>(command.length + 1);
			cmd.add(adb.getAbsolutePath());
			for (String c : command)
				cmd.add(c);
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			return IOUtils.toString(p.getInputStream());
		} catch (Exception e) {
			Database.logError(e);
			throw new RuntimeException(e);
		}
	}

}
