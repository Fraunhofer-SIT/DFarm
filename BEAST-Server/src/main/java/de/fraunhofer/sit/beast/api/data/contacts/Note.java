package de.fraunhofer.sit.beast.api.data.contacts;

import de.fraunhofer.sit.beast.api.data.contacts.EmailAddress.EmailType;
import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class Note implements IDataItem {
	public String note;
	
	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			note = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		return AndroidUtils.getBinding("data1", note) + AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/note");
	}
}
