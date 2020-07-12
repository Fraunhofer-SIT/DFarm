package de.fraunhofer.sit.beast.api.data.contacts;

//https://www.dev2qa.com/android-contacts-fields-data-table-columns-and-data-mimetype-explain/
public interface IDataItem {
	public void apply(String key, String value);

	String getBindings();
}
