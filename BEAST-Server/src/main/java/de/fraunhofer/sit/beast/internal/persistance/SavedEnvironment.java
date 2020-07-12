package de.fraunhofer.sit.beast.internal.persistance;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "SavedEnvironment")
public class SavedEnvironment {
	@DatabaseField(generatedId=true)
	public int id;
	
	@DatabaseField(uniqueCombo=true)
	public String name;

	@DatabaseField(uniqueCombo=true)
	public int device;

	@DatabaseField
	public String user;
}
