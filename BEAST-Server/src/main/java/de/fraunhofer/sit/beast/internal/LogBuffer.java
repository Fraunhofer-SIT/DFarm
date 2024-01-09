package de.fraunhofer.sit.beast.internal;

import java.util.concurrent.LinkedBlockingQueue;

import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatReceiverTask;

/**
 * @author controller Used to forward log messages from e.g. a LogCatListener to
 *         an OutputWriter. If the connection is closed, the {@link LogBuffer}
 *         can be closed using close().
 */
public class LogBuffer {
	private LinkedBlockingQueue<String> queue;
	private LogCatListener logrec;
	private LogCatReceiverTask logcatReceiver;

	public LogBuffer() {
		this.queue = new LinkedBlockingQueue<>();
	}

	public void close() {
		if (logcatReceiver != null)
			logcatReceiver.removeLogCatListener(logrec);
		logcatReceiver = null;
		logrec = null;
		queue = null;

	}

	public boolean isClosed() {
		return queue == null;
	}

	public void writeMessage(String msg) {
		if (queue == null)
			throw new IllegalStateException("Buffer Cancelled");
		queue.add(msg);

	}

	public String getMessage() throws InterruptedException {
		if (queue == null)
			throw new IllegalStateException("Buffer Cancelled");
		return queue.take();
	}

	public void setListener(LogCatReceiverTask logCatReceiver, LogCatListener logrec) {
		this.logcatReceiver = logCatReceiver;
		this.logrec = logrec;
	}
}
