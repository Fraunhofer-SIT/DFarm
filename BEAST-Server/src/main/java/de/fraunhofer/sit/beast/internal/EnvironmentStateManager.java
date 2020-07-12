package de.fraunhofer.sit.beast.internal;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;

import de.fraunhofer.sit.beast.api.data.devices.DeviceState;
import de.fraunhofer.sit.beast.api.data.exceptions.ExceptionProvider;
import de.fraunhofer.sit.beast.internal.android.resetters.AndroidApplicationEnvironmentResetter.AndroidApplicationResetInformation;
import de.fraunhofer.sit.beast.internal.interfaces.IDevice;
import de.fraunhofer.sit.beast.internal.interfaces.IEnvironmentResetter;
import de.fraunhofer.sit.beast.internal.persistance.CIJdbcConnectionSource;
import de.fraunhofer.sit.beast.internal.persistance.Database;
import de.fraunhofer.sit.beast.internal.persistance.SavedEnvironment;

public class EnvironmentStateManager {

	/**
	 * This special name indicates an environment which is currently saved.
	 */
	private static final String SAVING_IN_PROGRESS_STATE = "(in progress)";
	private static final Logger LOGGER = Logger.getLogger(EnvironmentStateManager.class);

	public static void setEnvironment(IDevice d, DeviceState stateAfterEnvironmentSet, String environment) throws SQLException {
		long msBefore = System.currentTimeMillis();
		LOGGER.info(String.format("Restoring environment of %s: %s", d.getDeviceInfo().getLongIdentifier(), environment));
		SavedEnvironment env = Database.INSTANCE.getSavedEnvironment(d.getDeviceInfo().ID, environment);
		d.changeState(DeviceState.PREPARING);
		Database.INSTANCE.updateDevice(d.getDeviceInfo());
		for (IEnvironmentResetter resetter : IEnvironmentResetter.getResetters(Database.INSTANCE)) {
			try {
				resetter.resetToKnownState(d, env);
			} catch (Throwable t) {
				Database.logError(d, t);
				d.changeState(DeviceState.ERROR);
				return;
			}
		}
		LOGGER.info(String.format("Restoring environment of %s (%s) completed, took %d ms", d.getDeviceInfo().getLongIdentifier(), environment, System.currentTimeMillis() - msBefore));
		d.changeState(stateAfterEnvironmentSet);
		
	}

	public static synchronized void saveEnvironmentState(IDevice dev, String saveName) throws SQLException {
		long start = System.currentTimeMillis();
		LOGGER.info(String.format("Saving environment of %s: %s", dev.getDeviceInfo().getLongIdentifier(), saveName));
		SavedEnvironment p = Database.INSTANCE.getSavedEnvironmentUnsafe(dev.getDeviceInfo().ID, SAVING_IN_PROGRESS_STATE);
		if (p != null) {
			deleteEnvironmentState(dev, p);
		}
		SavedEnvironment env = Database.INSTANCE.getSavedEnvironmentUnsafe(dev.getDeviceInfo().ID, saveName);
		if (env == null) {
			env = new SavedEnvironment();
			env.name = SAVING_IN_PROGRESS_STATE;
			env.device = dev.getDeviceInfo().ID;
		}
		env.user = dev.getDeviceInfo().reservedBy;
		CIJdbcConnectionSource jdbc = Database.INSTANCE.getConnectionSource();
		Dao<SavedEnvironment, ?> daoEnv = DaoManager.createDao(jdbc, SavedEnvironment.class);;
		daoEnv.createOrUpdate(env);
		
		for (IEnvironmentResetter resetter : IEnvironmentResetter.getResetters(Database.INSTANCE)) {
			try {
				//Just be sure:
				resetter.deleteKnownState(env);
				resetter.saveAsKnownState(dev, env);
			} catch (Throwable t) {
				Database.logError(dev, t);
				deleteEnvironmentState(dev, env);
				return;
			}
		}
		env.name = saveName;
		daoEnv.update(env);
		LOGGER.info(String.format("Environment saved of %s: %s, took %d ms", dev.getDeviceInfo().getLongIdentifier(), saveName, System.currentTimeMillis() - start));
	}

	public static void deleteEnvironmentState(IDevice dev, SavedEnvironment env) throws SQLException {
		for (IEnvironmentResetter resetter : IEnvironmentResetter.getResetters(Database.INSTANCE)) {
			try {
				resetter.deleteKnownState(env);
			} catch (Throwable t) {
				Database.logError(dev, t);
			}
		}
		Database.INSTANCE.deleteSavedEnvironment(env);
	}

	public static void loadEnvironmentState(IDevice dev, String environment) throws SQLException {
		setEnvironment(dev, dev.getDeviceInfo().state, environment);
	}

	public static final String DEFAULT_ENVIRONMENT = "Default";


}
