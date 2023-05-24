package de.fraunhofer.sit.beast.internal.annotations;

import java.lang.reflect.Field;

import de.fraunhofer.sit.beast.internal.utils.MainUtils;

public final class CommandBuilder {
	public static String getCommand(String base, Object options) throws IllegalArgumentException, IllegalAccessException {
		StringBuilder sb;
		if (base != null)
			 sb = new StringBuilder(base).append(" ");
		else
			sb = new StringBuilder();
		
		for (Field f : options.getClass().getDeclaredFields()) {
			CommandBuilderAnnotation a = f.getAnnotation(CommandBuilderAnnotation.class);
			if (a != null) {
				Object o = f.get(options);
				if (o != null) {
					if (f.getType() == boolean.class || f.getType() == Boolean.class) {
						if ((boolean)o)
							sb.append(a.commandName()).append(" ");
					}
					else 
						sb.append(a.commandName()).append(" ").append(MainUtils.escapeCommand(o.toString())).append(" ");
				}
			}
		}
		return sb.toString();
	}
}
