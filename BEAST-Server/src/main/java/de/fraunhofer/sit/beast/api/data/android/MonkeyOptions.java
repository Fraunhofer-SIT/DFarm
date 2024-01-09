package de.fraunhofer.sit.beast.api.data.android;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.fraunhofer.sit.beast.internal.annotations.CommandBuilder;
import de.fraunhofer.sit.beast.internal.annotations.CommandBuilderAnnotation;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Options for monkey application exerciser")
@JsonIgnoreProperties({ "commandSyntax" })
public class MonkeyOptions {
	@Schema(required = true, description = "The number of events")
	public int eventCount;

	@Schema(required = false, description = "The package Name of the application")
	public String[] packageNames;

	@Schema(required = false, description = "If you specify one or more categories this way, the Monkey will only allow the system to visit activities that are listed with one of the specified categories. If you don't specify any categories, the Monkey will select activities listed with the category Intent.CATEGORY_LAUNCHER or Intent.CATEGORY_MONKEY. To specify multiple categories, use the -c option multiple times - one -c option per category.")
	public String[] categories;

	@CommandBuilderAnnotation(commandName = "-s")
	@Schema(required = false, description = "The seed for the PRNG used to generate events")
	public Long seed;

	@CommandBuilderAnnotation(commandName = "--throttle")
	@Schema(required = false, description = "A fixed delay between events in milliseconds")
	public Integer throttleMs;

	@CommandBuilderAnnotation(commandName = "--pct-touch")
	@Schema(required = false, description = "Percentage of touch events (Touch events are a down-up event in a single place on the screen.)")
	public Integer percentageOfTouchEvents;

	@CommandBuilderAnnotation(commandName = "--pct-motion")
	@Schema(required = false, description = "Percentage of motion events (Motion events consist of a down event somewhere on the screen, a series of pseudo-random movements, and an up event.)")
	public Integer percentageOfMotionEvents;

	@CommandBuilderAnnotation(commandName = "--pct-trackball")
	@Schema(required = false, description = "Percentage of trackball events. (Trackball events consist of one or more random movements, sometimes followed by a click.)")
	public Integer percentageOfTrackballEvents;

	@CommandBuilderAnnotation(commandName = "--pct-nav")
	@Schema(required = false, description = "Percentage of basic navigation events. (Navigation events consist of up/down/left/right, as input from a directional input device.)")
	public Integer percentageOfBasicNavigationEvents;

	@CommandBuilderAnnotation(commandName = "--pct-majornav")
	@Schema(required = false, description = "Percentage of major navigation events. (These are navigation events that will typically cause actions within your UI, such as the center button in a 5-way pad, the back key, or the menu key.)")
	public Integer percentageOfMajorNavigationEvents;

	@CommandBuilderAnnotation(commandName = "--pct-syskeys")
	@Schema(required = false, description = "Percentage of \"system\" key events. (These are keys that are generally reserved for use by the system, such as Home, Back, Start Call, End Call, or Volume controls.)")
	public Integer percentageOfSystemNavigationEvents;

	@CommandBuilderAnnotation(commandName = "--pct-appswitch")
	@Schema(required = false, description = "Percentage of activity launches. At random intervals, the Monkey will issue a startActivity() call, as a way of maximizing coverage of all activities within your package.")
	public Integer percentageOfAppSwitchEvents;

	@CommandBuilderAnnotation(commandName = "--pct-anyevent")
	@Schema(required = false, description = "Percentage of other types of events. This is a catch-all for all other types of events such as keypresses, other less-used buttons on the device, and so forth.")
	public Integer percentageOfOtherEvents;

	@CommandBuilderAnnotation(commandName = "--dbg-no-events")
	@Schema(required = false, defaultValue = "false", description = "When specified, the Monkey will perform the initial launch into a test activity, but will not generate any further events. For best results, combine with -v, one or more package constraints, and a non-zero throttle to keep the Monkey running for 30 seconds or more. This provides an environment in which you can monitor package transitions invoked by your application.")
	public boolean dbgNoEvents;

	@CommandBuilderAnnotation(commandName = "--hprof")
	@Schema(required = false, defaultValue = "false", description = "If set, this option will generate profiling reports immediately before and after the Monkey event sequence. This will generate large (~5Mb) files in data/misc, so use with care.")
	public boolean generateHeapDumps;

	@CommandBuilderAnnotation(commandName = "--ignore-crashes")
	@Schema(required = false, defaultValue = "false", description = "Normally, the Monkey will stop when the application crashes or experiences any type of unhandled exception. If you specify this option, the Monkey will continue to send events to the system, until the count is completed.")
	public boolean ignoreCrashes;

	@CommandBuilderAnnotation(commandName = "--ignore-timeouts")
	@Schema(required = false, defaultValue = "false", description = "Normally, the Monkey will stop when the application experiences any type of timeout error such as a \"Application Not Responding\" dialog. If you specify this option, the Monkey will continue to send events to the system, until the count is completed.")
	public boolean ignoreTimeouts;

	@CommandBuilderAnnotation(commandName = "--ignore-security-exceptions")
	@Schema(required = false, defaultValue = "false", description = "Normally, the Monkey will stop when the application experiences any type of permissions error, for example if it attempts to launch an activity that requires certain permissions. If you specify this option, the Monkey will continue to send events to the system, until the count is completed.")
	public boolean ignoreSecurityExceptions;

	@CommandBuilderAnnotation(commandName = "--kill-process-after-error")
	@Schema(required = false, defaultValue = "false", description = "Normally, when the Monkey stops due to an error, the application that failed will be left running. When this option is set, it will signal the system to stop the process in which the error occurred. Note, under a normal (successful) completion, the launched process(es) are not stopped, and the device is simply left in the last state after the final event.")
	public boolean killProcessAfterError;

	@CommandBuilderAnnotation(commandName = "--monitor-native-crashes")
	@Schema(required = false, defaultValue = "false", description = "Watches for and reports crashes occurring in the Android system native code. If --kill-process-after-error is set, the system will stop.")
	public boolean monitorNativeCrashes;

	@CommandBuilderAnnotation(commandName = "--wait-dbg")
	@Schema(required = false, defaultValue = "false", description = "Starts monkey after a debugger is attached to it")
	public boolean waitForDebugger;

	public String getCommandSyntax() throws IllegalArgumentException, IllegalAccessException {
		String cb = CommandBuilder.getCommand("monkey", this);
		StringBuilder builder = new StringBuilder(cb);

		if (categories != null) {
			for (String category : categories) {
				builder.append(String.format("-c \"%s\" ", MainUtils.escapeCommand(category)));
			}
		}
		if (packageNames != null) {
			for (String packageName : packageNames) {
				builder.append(String.format("-p \"%s\" ", MainUtils.escapeCommand(packageName)));
			}
		}
		builder.append(" ").append(eventCount);
		return builder.toString();

	}
}
