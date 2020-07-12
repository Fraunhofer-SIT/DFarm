package de.fraunhofer.sit.beast.internal.persistance;

import java.lang.reflect.Field;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.EnumIntegerType;

import de.fraunhofer.sit.beast.api.data.devices.DeviceState;

public class DeviceStatePersister  extends EnumIntegerType {

	public static final int OCCUPIED = 3;
	public static final int PREPARING = 4;
	public static final int ERROR = 2;
	public static final int FREE = 1;
	public static final int DISCONNECTED = 0;

	public DeviceStatePersister() {
		super(SqlType.INTEGER, new Class[] { DeviceState.class });
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
		switch ((DeviceState) javaObject) {
		case DISCONNECTED:
			return DISCONNECTED;
		case ERROR:
			return ERROR;
		case FREE:
			return FREE;
		case OCCUPIED:
			return OCCUPIED;
		case PREPARING:
			return PREPARING;
		default:
			throw new IllegalArgumentException(String.format("Unknown device state: %s", javaObject));

		}
	}

	@Override
	public boolean isValidForField(Field field) {
		return super.isValidForField(field) && field.getType().equals(getAssociatedClasses()[0]);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		if (sqlArg == null) {
			return null;
		} else {
			switch ((int) sqlArg) {
			case DISCONNECTED:
				return DeviceState.DISCONNECTED;
			case ERROR:
				return DeviceState.ERROR;
			case FREE:
				return DeviceState.FREE;
			case OCCUPIED:
				return DeviceState.OCCUPIED;
			case PREPARING:
				return DeviceState.PREPARING;

			default:
				throw new IllegalArgumentException(String.format("Unknown device state: %d", sqlArg.toString()));

			}
		}
	}


}
