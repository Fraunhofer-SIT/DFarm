package de.fraunhofer.sit.beast.internal.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.fraunhofer.sit.beast.api.data.devices.DeviceInformation;
import de.fraunhofer.sit.beast.internal.interfaces.AbstractApp;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Schema(description = "Android Application information", allOf = { AbstractApp.class })
public class AndroidApp extends AbstractApp {

	
	public static final String COMPANION_APP_PACKAGE_NAME = "fraunhofer.sit.companionapp";

	@Schema(required = false, description = "The version code of the app", accessMode=AccessMode.READ_ONLY)
	public int versionCode;

	@Schema(required = false, description = "The target SDK version of the app", accessMode=AccessMode.READ_ONLY)
	public int targetSDK;

	@Schema(required = false, description = "The version name of the app", accessMode=AccessMode.READ_ONLY)
	public String versionName;

	@Schema(required = false, description = "The path where the app resides on the device", accessMode=AccessMode.READ_ONLY)
	public String codePath;

	@Schema(required = false, description = "The date of first installation", accessMode=AccessMode.READ_ONLY)
	@JsonFormat(pattern=MainUtils.DATE_FORMAT)
	public Date firstDateInstall;

	@Schema(required = false, description = "The date of last update", accessMode=AccessMode.READ_ONLY)
	@JsonFormat(pattern=MainUtils.DATE_FORMAT)
	public Date lastUpdateTime;

	@Schema(required = false, description = "The minimum sdk version", accessMode=AccessMode.READ_ONLY)
	public int minSDKVersion;

	@Schema(required = false, description = "Permission requested upon installation", accessMode=AccessMode.READ_ONLY)
	public List<AndroidPermission> installPermissions;
	@Schema(required = false, description = "Permission declared in the manifest", accessMode=AccessMode.READ_ONLY)
	public List<AndroidPermission> declaredPermissions;
	@Schema(required = false, description = "Permission requsted at runtime", accessMode=AccessMode.READ_ONLY)
	public List<AndroidPermission> runtimePermissions;

	@Schema(required = true, description = "Whether the app is a system app", accessMode=AccessMode.READ_ONLY)
	public boolean systemApp;

	@Schema(required = true, description = "Whether the backup is allowed", accessMode=AccessMode.READ_ONLY)
	public boolean allowBackup;

	public AndroidApp(String packageName) {
		super(packageName, "AndroidApp");
	}

	public String getPackageName() {
		return id;
	}

}
