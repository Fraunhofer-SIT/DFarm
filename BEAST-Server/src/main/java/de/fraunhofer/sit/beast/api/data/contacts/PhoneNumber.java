package de.fraunhofer.sit.beast.api.data.contacts;

import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class PhoneNumber implements IDataItem {
	public String number;
	
	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			number = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		return AndroidUtils.getBinding("data1", number) + AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/phone_v2");
	}

}
