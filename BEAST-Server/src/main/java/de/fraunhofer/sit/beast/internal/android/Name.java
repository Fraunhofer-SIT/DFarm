package de.fraunhofer.sit.beast.internal.android;

import de.fraunhofer.sit.beast.api.data.contacts.Contact;
import de.fraunhofer.sit.beast.api.data.contacts.IDataItem;

public class Name implements IDataItem {

	private Contact contact;

	public Name(Contact contact) {
		this.contact = contact;
	}

	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			contact.displayName = value;
			break;
		case "data2":
			contact.givenName = value;
			break;
		case "data3":
			contact.familyName = value;
			break;
		case "data5":
			contact.middleName = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		String bindings = AndroidUtils.getBinding("data1", contact.displayName);
		bindings += AndroidUtils.getBinding("data2", contact.givenName);
		bindings += AndroidUtils.getBinding("data3", contact.familyName);
		bindings += AndroidUtils.getBinding("data5", contact.middleName);
		bindings += AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/name");
		return bindings;
	}

}
