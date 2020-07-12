package de.fraunhofer.sit.beast.internal.interfaces;

import java.util.Collection;

import de.fraunhofer.sit.beast.api.data.network.ports.PortForwarding;

public interface IPortForwardingListing {
	public Collection<PortForwarding> listPortForwardings();
	public void removePortForwarding(PortForwarding forwarding);
	public void createPortForwarding(PortForwarding forwarding);
}
