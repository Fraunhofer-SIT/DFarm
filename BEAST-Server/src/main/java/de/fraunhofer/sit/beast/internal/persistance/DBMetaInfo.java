package de.fraunhofer.sit.beast.internal.persistance;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "DBMetaInfo")
public class DBMetaInfo {

	@DatabaseField(id = true)
	public final int id = 1;

	@DatabaseField(columnName = "lastID")
	public int lastID;

	@DatabaseField
	public int schemaVersion;

}
