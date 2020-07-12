package de.fraunhofer.sit.beast.api.data;

import java.io.FileNotFoundException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.fraunhofer.sit.beast.internal.interfaces.IFile;
import de.fraunhofer.sit.beast.internal.utils.MainUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Schema(description = "Information about a file")
public class FileOnDevice {
	@Schema(example = "foo.txt", required = true, description = "The short name of the file", accessMode=AccessMode.READ_ONLY)
	public String name;

	@Schema(example = "false", required = true, description = "Whether the file is directory", accessMode=AccessMode.READ_ONLY)
	public boolean directory;

	@Schema(example = "true", required = true, description = "Whether the file is a real file", accessMode=AccessMode.READ_ONLY)
	public boolean file;

	@Schema(example = "2048", required = false, description = "The size of the file in bytes", accessMode=AccessMode.READ_ONLY)
	public Long size;

	@Schema(required = false, description = "Last modification date", accessMode=AccessMode.READ_ONLY)
	@JsonFormat(pattern=MainUtils.DATE_FORMAT)
	public Date lastModified;

	@Schema(required = true, description = "Full path", accessMode=AccessMode.READ_ONLY)
	public String fullPath;

	
	public FileOnDevice() {
		
	}
	
	public FileOnDevice(IFile file) throws FileNotFoundException {
		this.directory = file.isDirectory();
		this.file = file.isFile();
		this.name = file.getShortName();
		this.fullPath = file.getFullPath();
		if (this.file && file.exists()) {
			this.size = file.getSize();
			this.lastModified = file.getLastModified();
		}
	}

}
