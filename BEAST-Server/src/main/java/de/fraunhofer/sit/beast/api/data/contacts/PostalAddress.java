package de.fraunhofer.sit.beast.api.data.contacts;

import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class PostalAddress implements IDataItem {
	public String country, region, street, formatted_address, city, neighborHood;
	public PostalType type;
	public String postalCode;

	public enum PostalType {
		HOME, OTHER, WORK
	}

	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			formatted_address = value;
			break;
		case "data2":
			switch (value) {
			case "1":
				type = PostalType.HOME;
				break;
			case "2":
				type = PostalType.WORK;
				break;
			case "3":
				type = PostalType.OTHER;
				break;
			}
			break;
		case "data4":
			street = value;
			break;
		case "data6":
			neighborHood = value;
			break;
		case "data7":
			city = value;
			break;
		case "data8":
			region = value;
			break;
		case "data9":
			postalCode = value;
			break;
		case "data10":
			country = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		int postalType = -1;
		if (type != null) {
			switch (type) {
			case HOME:
				postalType = 1;
				break;
			case WORK:
				postalType = 2;
				break;
			case OTHER:
				postalType = 3;
				break;
			}
		}
		return AndroidUtils.getBinding("data1", formatted_address) + AndroidUtils.getBinding("data4", street)
				+ AndroidUtils.getBinding("data6", neighborHood) + AndroidUtils.getBinding("data7", city)
				+ AndroidUtils.getBinding("data8", region) + AndroidUtils.getBinding("data9", postalCode)
				+ AndroidUtils.getBinding("data10", country)
				+ (postalType != -1 ? AndroidUtils.getBinding("data2", postalType) : "")
				+ AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/postal-address_v2");
	}

}
