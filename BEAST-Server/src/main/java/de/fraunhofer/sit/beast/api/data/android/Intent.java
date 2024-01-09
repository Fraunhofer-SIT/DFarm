package de.fraunhofer.sit.beast.api.data.android;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An Android Intent")
@JsonIgnoreProperties({ "commandSyntax" })
public class Intent {
	@Schema(required = true, description = "The action", example = "android.intent.action.MAIN")
	public String action;

	@Schema(required = false, description = "The category", example = "android.intent.category.LAUNCHER")
	public String category;

	@Schema(required = false, description = "The extras")
	public Map<String, String> extras;

	@Schema(required = false, description = "The component", example = "")
	public String component;

	@Schema(required = false, description = "The data uri", example = "")
	public String dataURI;

	@Schema(required = false, description = "The mime type", example = "")
	public String mimeType;

	@Schema(required = false, description = "Flags", example = "0")
	public int flags;

	public String getCommandSyntax() {
		StringBuilder builder = new StringBuilder();
		if (component != null)
			builder.append(String.format("-n \"%s\" ", MainUtils.escapeCommand(component)));
		if (category != null)
			builder.append(String.format("-c \"%s\" ", MainUtils.escapeCommand(category)));

		if (action != null)
			builder.append(String.format("-a \"%s\" ", MainUtils.escapeCommand(action)));
		if (dataURI != null)
			builder.append(String.format("-d \"%s\" ", MainUtils.escapeCommand(dataURI)));
		if (mimeType != null && !mimeType.isEmpty())
			builder.append(String.format("-t \"%s\" ", MainUtils.escapeCommand(mimeType)));
		builder.append(String.format("-f %d ", flags));
		return builder.toString();

	}
}
