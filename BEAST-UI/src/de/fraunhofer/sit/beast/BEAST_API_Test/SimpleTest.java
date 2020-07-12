package de.fraunhofer.sit.beast.BEAST_API_Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.fraunhofer.sit.beast.client.api.AppsApi;
import de.fraunhofer.sit.beast.client.api.DeviceEnvironmentsApi;
import de.fraunhofer.sit.beast.client.api.DevicesApi;
import de.fraunhofer.sit.beast.client.api.FileSystemApi;
import de.fraunhofer.sit.beast.client.api.InputApi;
import de.fraunhofer.sit.beast.client.invoker.ApiClient;
import de.fraunhofer.sit.beast.client.invoker.ApiException;
import de.fraunhofer.sit.beast.client.invoker.Configuration;
import de.fraunhofer.sit.beast.client.invoker.auth.ApiKeyAuth;
import de.fraunhofer.sit.beast.client.models.AbstractApp;
import de.fraunhofer.sit.beast.client.models.AndroidDeviceRequirements;
import de.fraunhofer.sit.beast.client.models.DeviceInformation;
import de.fraunhofer.sit.beast.client.models.FileOnDevice;
import de.fraunhofer.sit.beast.client.models.Intent;

/**
 * Hello world!
 *
 */
public class SimpleTest 
{
    public static void main( String[] args ) throws ApiException
    {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://pc-sse-handycontroller.sit.fraunhofer.de:5080");
        //defaultClient.getHttpClient().setProxy(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)));
        // Configure API key authorization: APIKey
        ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
        APIKey.setApiKey("YOUR API KEY");

        defaultClient.getHttpClient().setReadTimeout(1, TimeUnit.DAYS);
         
        DevicesApi devAPI = new DevicesApi(defaultClient);
        AppsApi appsAPI = new AppsApi(defaultClient);
        DeviceEnvironmentsApi envAPI = new DeviceEnvironmentsApi(defaultClient);
        FileSystemApi fsAPI = new FileSystemApi(defaultClient);
        InputApi inputAPI = new InputApi(defaultClient);
        
        
        List<DeviceInformation> devices = devAPI.getDevices(new AndroidDeviceRequirements());
        System.out.println(devices);
        devAPI.releaseAllDevices(); 
        DeviceInformation reserved = devAPI.reserveDevice(new AndroidDeviceRequirements());
        List<AbstractApp> appsInstalled = appsAPI.getInstalledApps(reserved.getID());
        System.out.println(appsInstalled);
        System.out.println(reserved);
        for (FileOnDevice file : fsAPI.listFiles(reserved.getID(), "/sdcard/")) {
        	System.out.println(file);
        	if (file.getFile()) {
        		File x = fsAPI.download(reserved.getID(), file.getFullPath());
        		System.out.println(x);
        	}
        }
        
        System.out.println(envAPI.listStates(reserved.getID()));
        inputAPI.typeText(reserved.getID(), "Test");
        launchIntent(inputAPI, reserved.getID());
        devAPI.releaseDevice(reserved.getID());
    }

	private static void launchIntent(InputApi inputAPI, int id) throws ApiException {
		Intent intent = new Intent();
		intent.action("android.intent.action.VIEW").dataURI("http://codeinspect.de");
		inputAPI.startActivity(id, false, false, intent);
		System.out.println();
		
	}
}
