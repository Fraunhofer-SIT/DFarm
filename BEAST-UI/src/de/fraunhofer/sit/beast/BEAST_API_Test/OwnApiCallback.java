package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.util.List;
import java.util.Map;

import de.fraunhofer.sit.beast.client.invoker.ApiCallback;
import de.fraunhofer.sit.beast.client.invoker.ApiException;



public abstract class OwnApiCallback<T> implements ApiCallback<T> {

	public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
		DeviceChooser.handleError(e);
	}


	public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
	}

	public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
	}

}
