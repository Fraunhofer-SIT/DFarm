package de.fraunhofer.sit.beast.applications;

import java.util.HashSet;
import java.util.Set;

import de.fraunhofer.sit.beast.api.operations.*;
import jakarta.ws.rs.core.Application;

public class ApplicationConfig extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> tmp = new HashSet<Class<?>>();
		tmp.add(org.jboss.resteasy.plugins.providers.ByteArrayProvider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.DefaultTextPlain.class);
		tmp.add(org.jboss.resteasy.plugins.providers.DefaultBooleanWriter.class);
		tmp.add(org.jboss.resteasy.plugins.providers.DefaultNumberWriter.class);
		tmp.add(org.jboss.resteasy.plugins.providers.FileProvider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.FileRangeWriter.class);
		tmp.add(org.jboss.resteasy.plugins.providers.jaxb.MapProvider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.MapMultipartFormDataWriter.class);
		tmp.add(org.jboss.resteasy.plugins.providers.InputStreamProvider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.jaxb.CollectionProvider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.MultipartReader.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataReader.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.MultipartFormAnnotationReader.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedReader.class);
		tmp.add(org.jboss.resteasy.plugins.providers.multipart.ListMultipartReader.class);
		tmp.add(org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider.class);
		for (Class<?> c : getMyClasses())
			tmp.add(c);
		return tmp;
	}

	public Set<Class<?>> getMyClasses() {
		HashSet<Class<?>> tmp = new HashSet<Class<?>>();
		tmp.add(Apps.class);
		tmp.add(Contacts.class);
		tmp.add(DeviceEnvironments.class);
		tmp.add(Information.class);
		tmp.add(Devices.class);
		tmp.add(FileSystem.class);
		tmp.add(Input.class);
		tmp.add(Network.class);
		tmp.add(Ports.class);
		return tmp;
	}

}
