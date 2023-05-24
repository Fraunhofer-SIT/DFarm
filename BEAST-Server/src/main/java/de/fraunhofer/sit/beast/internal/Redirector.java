package de.fraunhofer.sit.beast.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is responsible to forward exactly one port.
 */
public class Redirector {
	static final Logger LOGGER = LogManager.getLogger(Redirector.class);
	
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
	
	public ServerSocket getSocket() {
		return socket;
	}
}