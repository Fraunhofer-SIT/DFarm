package de.fraunhofer.sit.beast.api.data.contacts;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.fraunhofer.sit.beast.api.data.exceptions.ExceptionProvider;
import de.fraunhofer.sit.beast.internal.android.AndroidUtils;

public class IMAddress implements IDataItem {

	public String address;
	public String protocol;
	private static final Logger logger = Logger.getLogger(IMAddress.class);
	
	private static final Map<Integer, String> PROTOCOLS = new HashMap<>();
	static {
		PROTOCOLS.put(0, "AIM");
		PROTOCOLS.put(1, "MSN");
		PROTOCOLS.put(2, "Yahoo");
		PROTOCOLS.put(3, "Skype");
		PROTOCOLS.put(4, "QQ");
		PROTOCOLS.put(5, "Google Talk");
		PROTOCOLS.put(6, "ICQ");
		PROTOCOLS.put(7, "Jabber");
		PROTOCOLS.put(8, "Netmeeting");
	}
	
	
	@Override
	public void apply(String key, String value) {
		switch (key) {
		case "data6":
			if (!value.equals("NULL") && !value.isEmpty()) {
				protocol = value;
			}
		case "data5":
			int x = Integer.valueOf(value);
			//Protocol
			//-1 = Custom
			if (x == -1) {
				break;
			}
			protocol = PROTOCOLS.get(x);
			if (protocol == null)
			{
				protocol = "Unknown";
				logger.warn(String.format("Unknown id: %d", x));
			}
			break;
			
		}
	}

	@Override
	public String getBindings() {
		for (Entry<Integer, String> entry : PROTOCOLS.entrySet()) {
			if (entry.getValue().toLowerCase().equals(protocol.toLowerCase())) {
				return AndroidUtils.getBinding("data5", entry.getKey());
			}
		}
		return AndroidUtils.getBinding("data6", protocol) + AndroidUtils.getBinding("data5", -1) +
				 AndroidUtils.getBinding("mimetype", "vnd.android.cursor.item/im");
	}

}
