package de.fraunhofer.sit.beast.internal.interfaces;

public interface IFileListing {

	public IFile getRoot();

	public IFile getFile(String path);

	public IFile getFile(String[] path);
}
