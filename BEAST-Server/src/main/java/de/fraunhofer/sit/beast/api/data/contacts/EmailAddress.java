package de.fraunhofer.sit.beast.api.data.contacts;

import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class EmailAddress implements IDataItem {

	public String address, displayName;
	public EmailType type;

	public enum EmailType {
		HOME, MOBILE, OTHER, WORK
	}

	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			address = value;
			break;
		case "data2":
			switch (value) {
			case "1":
				type = EmailType.HOME;
				break;
			case "2":
				type = EmailType.WORK;
				break;
			case "3":
				type = EmailType.OTHER;
				break;
			case "4":
				type = EmailType.MOBILE;
				break;
			}
			break;
		case "data4":
			displayName = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		int otype = -1;
		if (type != null) {
			switch (type) {
			case HOME:
				otype = 1;
				break;
			case MOBILE:
				otype = 2;
				break;
			case OTHER:
				otype = 3;
				break;
			case WORK:
				otype = 4;
				break;

			}
		}
		return AndroidUtils.getBinding("data1", address) + (otype != -1 ? "" : AndroidUtils.getBinding("data2", otype))
				+ AndroidUtils.getBinding("data4", displayName)
				+ AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/email_v2");
	}

}
