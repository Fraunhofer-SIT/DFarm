package de.fraunhofer.sit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.models.OpenAPI;


/**
 * Sadly, we have not found a way yet to generate a YAML or JSON OpenAPI 3.0 file
 * from Swagger 2.0 (!!!) annotations. The maven plug-in currently only supports 1.5 annotations...
 * @author Marc Miltenberger
 */
public class YamlHelper {
	
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("First arg: Output file path");
			System.err.println("Second arg: Output file name");
			System.err.println("More args: Class name of API file");
			System.exit(1);
		}
		boolean typescriptHack = false;
		List<String> arg = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--typescripthack":
				typescriptHack = true;
				continue;
			}
			arg.add(args[i]);
		}
		args = arg.toArray(new String[0]);
		

		Reader reader = new Reader(new OpenAPI());

		Set<Class<?>> classes = new LinkedHashSet<>();
		for (int i = 2; i < args.length; i++)
			classes.add(Class.forName(args[i]));
		OpenAPI openAPI = reader.read(classes);
		Yaml.mapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
		final File out = new File(args[0]);
		System.out.println(String.format("Write to %s", out));
		String s = Json.pretty().writeValueAsString(openAPI);
		JsonNode node = Json.mapper().readTree(s);
		JsonNode schemas = node.get("components").get("schemas");
		Iterator<Entry<String, JsonNode>> schemaFields = schemas.fields();
		while (schemaFields.hasNext()) {
			Entry<String, JsonNode> entry = schemaFields.next();
			String className = entry.getKey();
			//Schema originalSchema = openAPI.getComponents().getSchemas().get(className);
			JsonNode allOf = entry.getValue().get("allOf");
			
			if (allOf != null) {
				if (typescriptHack) {
					//For typescript, we want to add all properties as is
					//and remove the inheritance relationship on this side, since otherwise
					//we end up with an empty interface and the additional properties
					//are lost completely bc the angular code gen is bad.
					//For dynamic typed languages, this should be OK.
					//Remove allOf
					((ObjectNode)entry.getValue()).remove("allOf");
					continue;
				} 
				JsonNode superClass = allOf.get(0);
				JsonNode r = superClass.get("$ref");
				JsonNode superC = node.at(r.asText().substring(1));
				
				JsonNode ownProperties = entry.getValue().get("properties");

				System.out.println("Fixing " + className);	
				Iterator<Entry<String, JsonNode>> it = ownProperties.fields();
				while (it.hasNext())
					if (superC.get("properties").get(it.next().getKey()) != null)
						//remove duplicate properties
						it.remove();
				
				((ObjectNode)entry.getValue()).remove("properties");
				ObjectNode objNode = Yaml.mapper().createObjectNode();
				objNode.set("properties", ownProperties);
				((ArrayNode)allOf).add(objNode);
			}
		}
			
		write(new File(out, args[1] + ".json"), Json.pretty().writeValueAsString(node));
		//s = Yaml.pretty().writeValueAsString(openAPI);
		//write(new File(out, args[1] + ".yaml"), s);
	}

	private static void write(File file, String s) throws IOException {
		file.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(s.getBytes("UTF-8"));
		} finally {
			fos.close();
		}
	}



}
