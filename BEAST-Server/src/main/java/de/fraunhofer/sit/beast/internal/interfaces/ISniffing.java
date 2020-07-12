package de.fraunhofer.sit.beast.internal.interfaces;

public interface ISniffing {

	public void startSniffing(int timeout) throws Exception;

	public void stopSniffing();

}
