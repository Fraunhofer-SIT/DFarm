package de.fraunhofer.sit.beast.internal.interfaces;

import java.sql.SQLException;

import de.fraunhofer.sit.beast.internal.android.resetters.AndroidApplicationEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.android.resetters.AndroidFileSystemEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;
import de.fraunhofer.sit.beast.internal.resetters.PortForwardingEnvironmentResetter;

public interface IEnvironmentResetter {
	public static IEnvironmentResetter[] getResetters(Database db) throws SQLException {
		return new IEnvironmentResetter[] { new AndroidFileSystemEnvironmentResetter(db),
				new AndroidApplicationEnvironmentResetter(db), new PortForwardingEnvironmentResetter(db) };
	}

	public void resetToKnownState(IDevice device, SavedEnvironment env) throws Exception;

	public void saveAsKnownState(IDevice device, SavedEnvironment env) throws Exception;

	/**
	 * Deletes a already saved state. This method must not throw an exception even
	 * if the saved environment has no associated data.
	 * 
	 * @param env
	 * @throws SQLException
	 */
	public void deleteKnownState(SavedEnvironment env) throws SQLException;
}
