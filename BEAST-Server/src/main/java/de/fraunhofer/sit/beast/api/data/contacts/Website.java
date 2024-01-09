package de.fraunhofer.sit.beast.api.data.contacts;

import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class Website implements IDataItem {
	public String URL;

	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data1":
			URL = value;
			break;
		}
	}

	@Override
	public String getBindings() {
		return AndroidUtils.getBinding("data1", URL)
				+ AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/website");
	}

}
