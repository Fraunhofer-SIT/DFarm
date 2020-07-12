package de.fraunhofer.sit.beast.internal.interfaces;

import java.util.Collection;

import de.fraunhofer.sit.beast.api.data.contacts.Contact;


public interface IContactListing {
	public Collection<Contact> getContacts();
	
	public void insertOrUpdateContact(Contact contact);
	
	public void deleteContact(int contactid);
}
