package de.fraunhofer.sit.beast.internal.persistance;

import java.util.Date;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Errors")
public class Error {
	@DatabaseField(columnName = "device_id")
	public int deviceInformationID;

	@DatabaseField(columnName = "text", uniqueCombo = true)
	public String text;

	@DatabaseField(columnName = "stackTrace", uniqueCombo = true)
	public String stackTrace;

	@DatabaseField(columnName = "timestamp")
	public Date timestamp = new Date();

	@DatabaseField(id = true, columnName = "id")
	public String id = UUID.randomUUID().toString();

	@DatabaseField(columnName = "sent")
	public boolean sent;
}
