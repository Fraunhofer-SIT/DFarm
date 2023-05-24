package de.fraunhofer.sit.beast.api.data.network.ports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;

import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;

@JsonIgnoreProperties({ "env"})
public class PortForwarding {
	@DatabaseField
	public int id = -1;
	
	@DatabaseField
	public int portOnHostMachine = -1;
	
	@DatabaseField
	public int portOnDevice;
	
	@DatabaseField
	public ForwardingDirection direction;

	@DatabaseField
	public Protocol protocolOnDevice;
	
	@DatabaseField(index=true, foreign=true)
	public SavedEnvironment env;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + portOnDevice;
		result = prime * result + portOnHostMachine;
		result = prime * result + ((protocolOnDevice == null) ? 0 : protocolOnDevice.hashCode());
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
		PortForwarding other = (PortForwarding) obj;
		if (direction != other.direction)
			return false;
		if (portOnDevice != other.portOnDevice)
			return false;
		if (portOnHostMachine != other.portOnHostMachine)
			return false;
		if (protocolOnDevice != other.protocolOnDevice)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Port On Host Machine=" + portOnHostMachine + "\nPort On Device=" + portOnDevice + "\nDirection=" + direction
				+ "\nProtocol On Device=" + protocolOnDevice + "\nId=" + id;
	}
	
	
	
	
}
