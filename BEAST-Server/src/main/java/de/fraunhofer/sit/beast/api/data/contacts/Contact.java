package de.fraunhofer.sit.beast.api.data.contacts;

import java.util.Date;
import java.util.List;

public class Contact {
	public int id = -1;
	public String familyName, givenName, middleName;
	public List<PhoneNumber> phoneNumbers;
	public List<EmailAddress> emailAddresses;
	public List<Note> notes;
	public List<Website> websites;
	public List<PostalAddress> postalAddresses;
	public List<IMAddress> imAddresses;
	public int timesContacted;
	public Date lastContact;
	public String displayName;
}
