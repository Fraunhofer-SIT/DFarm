package de.fraunhofer.sit.beast.internal.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;

public interface IFile {
	public String getShortName();

	public boolean isDirectory();

	public boolean isFile();

	public long getSize();

	public boolean exists();

	public InputStream openRead() ;

	public OutputStream openWrite() ;

	public void deleteRecursively() ;

	public void mkdirs() ;

	public List<IFile> listFiles() ;

	public String[] getPathParts();

	public String getMD5Sum();

	public void download(File file);

	public void upload(File file);

	public IFile getParent();

	public String getFullPath();

	public Date getLastModified();

}
