package de.fraunhofer.sit.beast.internal.interfaces;

import de.fraunhofer.sit.beast.api.data.exceptions.APIException;

public interface IFileListing {

	public IFile getRoot();

	public IFile getFile(String path) ;

	public IFile getFile(String[] path) ;
}
