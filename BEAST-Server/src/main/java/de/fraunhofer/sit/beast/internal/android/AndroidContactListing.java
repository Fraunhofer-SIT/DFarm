package de.fraunhofer.sit.beast.internal.android;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.fraunhofer.sit.beast.api.data.contacts.Contact;
import de.fraunhofer.sit.beast.api.data.contacts.EmailAddress;
import de.fraunhofer.sit.beast.api.data.contacts.IDataItem;
import de.fraunhofer.sit.beast.api.data.contacts.IMAddress;
import de.fraunhofer.sit.beast.api.data.contacts.Note;
import de.fraunhofer.sit.beast.api.data.contacts.PhoneNumber;
import de.fraunhofer.sit.beast.api.data.contacts.PostalAddress;
import de.fraunhofer.sit.beast.api.data.contacts.Website;
import de.fraunhofer.sit.beast.api.data.exceptions.APIException;
import de.fraunhofer.sit.beast.api.data.exceptions.APIExceptionWrapper;
import de.fraunhofer.sit.beast.internal.interfaces.IContactListing;
import de.fraunhofer.sit.beast.internal.persistance.Database;

public class AndroidContactListing implements IContactListing {
	private static final Logger logger = LogManager.getLogger(AndroidContactListing.class);

	private AndroidDevice device;
	
	private static final String GET_CONTACTS = "content query --uri content://com.android.contacts/data";
	private static final String GET_RAW_CONTACTS = "content query --uri content://com.android.contacts/raw_contacts";
	private static final String DELETE_CONTACT_COMPLETELY = "content delete --uri content://com.android.contacts/data --where contact_id=%s";
	private static final String DELETE_CONTACT_DATA_COMPLETELY = "content delete --uri content://com.android.contacts/raw_contacts --where contact_id=%s";

	public AndroidContactListing(AndroidDevice androidDevice) {
		this.device = androidDevice;
	}

	@Override
	public Collection<Contact> getContacts() {
		String contacts = device.executeOnDevice(GET_CONTACTS);
		String rawContacts = device.executeOnDevice(GET_RAW_CONTACTS);
		Map<Integer, Contact> result = new HashMap<>();
		try {
			contact:
			for (String contact : IOUtils.readLines(new StringReader(rawContacts))) {
				Contact c = new Contact();
				String[] split = contact.split(", ");
				int id = -1;
				for (String pair : split) {
					int idx = pair.indexOf('=');
					if (idx == -1)
						continue;
					String key = pair.substring(0, idx);
					String value = pair.substring(idx + 1);
					switch (key) {
					case "contact_id":
						try {
							id = Integer.valueOf(value);
						} catch (Exception e) {
							continue contact;
						}
						c.id = id;
						result.put(id, c);
						break;
					case "deleted":
						if (value.equals("1"))
							result.remove(id);
						break;
					case "times_contacted":
						c.timesContacted = Integer.valueOf(value);
						break;
					case "last_time_contacted":
						if (!value.equals("NULL"))
							c.lastContact = new Date(Long.valueOf(value));
						break;
					}
				}
			}
			for (String rawcontact : IOUtils.readLines(new StringReader(contacts))) {
				String[] split = rawcontact.split(", ");
				String mimeType = null;
				Contact contact = null;
				for (String pair : split) {
					if (pair.startsWith("contact_id=")) {
						String cid = pair.substring(11);
						int id = Integer.valueOf(cid);
						contact = result.get(id);
					} else if (pair.startsWith("mimetype=")) {
						mimeType = pair.substring(9);
					}
				}
				if (contact == null || mimeType == null)
					continue;
				IDataItem obj;
				switch (mimeType) {
				case "vnd.android.cursor.item/email_v2":
					obj = new EmailAddress();
					if (contact.emailAddresses == null)
						contact.emailAddresses = new ArrayList<>();
						
					contact.emailAddresses.add((EmailAddress) obj);
					break;
				case "vnd.android.cursor.item/im":
					obj = new IMAddress();
					if (contact.imAddresses == null)
						contact.imAddresses = new ArrayList<>();
						
					contact.imAddresses.add((IMAddress) obj);
					break;
				case "vnd.android.cursor.item/phone_v2":
					if (contact.phoneNumbers == null)
						contact.phoneNumbers = new ArrayList<>();
						
					obj = new PhoneNumber();
					contact.phoneNumbers.add((PhoneNumber) obj);
					break;
				case "vnd.android.cursor.item/postal-address_v2":
					if (contact.postalAddresses == null)
						contact.postalAddresses = new ArrayList<>();
						
					obj = new PostalAddress();
					contact.postalAddresses.add((PostalAddress) obj);
					break;
				case "vnd.android.cursor.item/website":
					if (contact.websites == null)
						contact.websites = new ArrayList<>();
						
					obj = new Website();
					contact.websites.add((Website) obj);
					break;
				case "vnd.android.cursor.item/note":
					if (contact.notes == null)
						contact.notes = new ArrayList<>();
						
					obj = new Note();
					contact.notes.add((Note) obj);
					break;
				case "vnd.android.cursor.item/name":
					obj = new Name(contact);
					break;
				default:
					logger.error(String.format("%s is a not supported mimetype", mimeType));
					continue;
				}
				
				for (int i = 0; i < split.length; i++) {
					String pair = split[i];
					int idx = pair.indexOf('=');
					String key = pair.substring(0, idx);
					String value = pair.substring(idx + 1);
					//Overhanging values:
					while (i + 1 < split.length && !split[i + 1].contains("=")) {
						value += ", " + split[++i];
					}
					obj.apply(key, value);
				}
			}
		} catch (IOException e) {
			Database.logError(e);
		}
		return result.values();
	}

	@Override
	public void insertOrUpdateContact(Contact contact) {
		String bindings = "";
		String cmd;
		if (contact.id == -1) {
			int maxContactId = 0;
			for (Contact c : getContacts()) {
				if (c.id > maxContactId)
					maxContactId = c.id;
			}
			contact.id = maxContactId + 1;
			bindings = " --bind contact_id:i:" + contact.id + " --bind raw_contact_id:i:" + contact.id ;
			cmd = "content insert --uri content://com.android.contacts/raw_contacts --bind deleted:i:0 "  + new Name(contact).getBindings() + bindings;
			device.executeThrowOutput(cmd);
		} else {
			bindings = " --bind contact_id:i:" + contact.id;
			cmd = "content update --uri content://com.android.contacts/raw_contacts --bind raw_contact_id:i:" + contact.id  + new Name(contact).getBindings();
			device.executeThrowOutput(cmd);
		}

		if (contact.emailAddresses != null) {
			for (EmailAddress o : contact.emailAddresses) {
				device.executeThrowOutput("content insert --uri content://com.android.contacts/data " + o.getBindings() + bindings);
			}
		}
		if (contact.phoneNumbers != null) {
			for (PhoneNumber o : contact.phoneNumbers) {
				device.executeThrowOutput("content insert --uri content://com.android.contacts/data " + o.getBindings() + bindings);
			}
		}
		
	}

	@Override
	public void deleteContact(int contactid) {
		if (contactid == -1)
			throw new NotFoundException(String.format("Contact not found"));
		device.executeThrowOutput(String.format(DELETE_CONTACT_DATA_COMPLETELY, contactid));
		device.executeThrowOutput(String.format(DELETE_CONTACT_COMPLETELY, contactid));
	}

}
