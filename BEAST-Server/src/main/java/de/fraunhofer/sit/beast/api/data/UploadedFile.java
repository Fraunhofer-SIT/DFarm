package de.fraunhofer.sit.beast.api.data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This file is just for swagger and represents a file uploaded to the server.
 * @author Marc Miltenberger
 */
public class UploadedFile {
	@Schema(required = true, description = "A file", type = "string", format = "binary")
	public String file;
}
