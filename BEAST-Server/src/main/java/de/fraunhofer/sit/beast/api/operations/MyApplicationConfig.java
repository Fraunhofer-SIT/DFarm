package de.fraunhofer.sit.beast.api.operations;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class MyApplicationConfig extends Application {

	private static final Set<Class<?>> CLASSES;

	static {
		HashSet<Class<?>> tmp = new HashSet<Class<?>>();
		tmp.add(Devices.class);
		tmp.add(DeviceEnvironments.class);
		tmp.add(FileSystem.class);
		tmp.add(Input.class);
		tmp.add(Apps.class);
		tmp.add(Contacts.class);
		tmp.add(Ports.class);

		CLASSES = Collections.unmodifiableSet(tmp);
	}

	@Override
	public Set<Class<?>> getClasses() {

		return CLASSES;
	}

}