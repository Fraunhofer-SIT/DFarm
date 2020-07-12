package de.fraunhofer.sit.beast.internal.resetters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import de.fraunhofer.sit.beast.api.data.network.ports.PortForwarding;
import de.fraunhofer.sit.beast.internal.android.AndroidApp;
import de.fraunhofer.sit.beast.internal.android.AndroidDevice;
import de.fraunhofer.sit.beast.internal.android.resetters.AndroidApplicationEnvironmentResetter.AndroidApplicationResetInformation;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.interfaces.IFileListing;
import de.fraunhofer.sit.beast.internal.interfaces.IPortForwardingListing;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;

/**
 * Prepares the device so that only a fixed set of port forwardings is available.
 * @author Marc Miltenberger
 */
public class PortForwardingEnvironmentResetter implements IEnvironmentResetter {
	private static final Logger LOGGER = LogManager.getLogger(PortForwardingEnvironmentResetter.class);
	
	private Dao<PortForwarding, ?> daoPortForwarding;
	
	public PortForwardingEnvironmentResetter(Database db) throws SQLException {
		daoPortForwarding = DaoManager.createDao(db.getConnectionSource(), PortForwarding.class);;
		TableUtils.createTableIfNotExists(db.getConnectionSource(), PortForwarding.class);
	}

	@Override
	public void resetToKnownState(IDevice device, SavedEnvironment env) throws SQLException, FileNotFoundException {
		QueryBuilder<PortForwarding, ?> db = daoPortForwarding.queryBuilder();
		db.where().eq("env_id", env.id);
		CloseableIterator<PortForwarding> it = db.iterator();
		try {
			Set<PortForwarding> contained = new HashSet<>();
			while (it.hasNext()) {
				PortForwarding fwd = it.next();
				contained.add(fwd);
			}
			IPortForwardingListing l = device.getPortFowardings();
			for (PortForwarding fwd : l.listPortForwardings()) {
				if (!contained.remove(fwd)) {
					LOGGER.info(String.format("Remove port forwarding %s on %s", fwd, device));
					l.removePortForwarding(fwd);
				}
			}
			for (PortForwarding fwd : contained)
			{
				LOGGER.info(String.format("Create port forwarding %s on %s", fwd, device));
				l.createPortForwarding(fwd);
			}
		} finally {
			it.closeQuietly();
		}
			
	}

	@Override
	public void saveAsKnownState(IDevice device, SavedEnvironment env) throws SQLException, IOException {
		deleteKnownState(env);
		for (PortForwarding fwd : device.getPortFowardings().listPortForwardings()) {
			int idBefore = fwd.id;
			daoPortForwarding.create(fwd);
			fwd.id = idBefore;
		}
	}

	@Override
	public void deleteKnownState(SavedEnvironment env) throws SQLException {
		DeleteBuilder<PortForwarding, ?> db = daoPortForwarding.deleteBuilder();
		db.where().eq("env_id", env.id);
		db.delete();
	}


}
