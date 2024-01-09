package de.fraunhofer.sit.beast.internal.android;

public class AndroidPermission {
	public String name;
	public boolean granted;

	public AndroidPermission(String name) {
		this.name = name;
	}

	public static AndroidPermission parsePermission(String entry) {
		int idx = entry.indexOf(":");
		if (idx == -1)
			throw new RuntimeException(String.format("%s does not contain :", entry));
		String name = entry.substring(0, idx);
		AndroidPermission perm = new AndroidPermission(name);
		String[] kvPairs = entry.substring(idx + 1).split(", ");
		for (String i : kvPairs) {
			String[] pair = i.split("=");
			switch (pair[0].trim()) {
			case "granted":
				perm.granted = pair[1].trim().equals("true");
				break;
			}
		}
		return perm;
	}

	@Override
	public String toString() {
		return name + (granted ? " Granted" : "Not granted");
	}

}
