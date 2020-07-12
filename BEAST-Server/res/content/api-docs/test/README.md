# swagger-java-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-java-client</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "io.swagger:swagger-java-client:1.0.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/swagger-java-client-1.0.0.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java
import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.AppsApi;

import java.io.File;
import java.util.*;

public class AppsApiExample {

    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: APIKey
        ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
        APIKey.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //APIKey.setApiKeyPrefix("Token");

        AppsApi apiInstance = new AppsApi();
        Integer devid = 56; // Integer | The id of device
        String appid = "appid_example"; // String | The id of app
        try {
            AbstractApp result = apiInstance.getInstalledAppInfo(devid, appid);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppsApi#getInstalledAppInfo");
            e.printStackTrace();
        }
    }
}
```

## Documentation for API Endpoints

All URIs are relative to */*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AppsApi* | [**getInstalledAppInfo**](docs/AppsApi.md#getInstalledAppInfo) | **GET** /api/devices/{devid}/apps/{appid} | Shows information about a specific app
*AppsApi* | [**getInstalledApps**](docs/AppsApi.md#getInstalledApps) | **GET** /api/devices/{devid}/apps | Lists installed apps
*AppsApi* | [**installApplication**](docs/AppsApi.md#installApplication) | **POST** /api/devices/{devid}/apps/installApplication | Installs an app
*AppsApi* | [**uninstallApplication**](docs/AppsApi.md#uninstallApplication) | **DELETE** /api/devices/{devid}/apps/{appid} | Uninstalls an app
*DeviceEnvironmentsApi* | [**listStates**](docs/DeviceEnvironmentsApi.md#listStates) | **GET** /api/devices/{devid}/environments | Returns a list of saved states
*DeviceEnvironmentsApi* | [**loadState**](docs/DeviceEnvironmentsApi.md#loadState) | **GET** /api/devices/{devid}/environments/{saveName}/load | Loads the saved state
*DeviceEnvironmentsApi* | [**saveState**](docs/DeviceEnvironmentsApi.md#saveState) | **PUT** /api/devices/{devid}/environments/{saveName}/save | Saves the current state as the new saved state
*DevicesApi* | [**getDevices**](docs/DevicesApi.md#getDevices) | **POST** /api/devices | Returns a list of devices
*DevicesApi* | [**getScreenshot**](docs/DevicesApi.md#getScreenshot) | **GET** /api/devices/{devid}/screenshot | Downloads a screenshot
*DevicesApi* | [**releaseAllDevices**](docs/DevicesApi.md#releaseAllDevices) | **GET** /api/devices/releaseAll | Release all devices reserved by the API key
*DevicesApi* | [**releaseDevice**](docs/DevicesApi.md#releaseDevice) | **GET** /api/devices/{devid}/release | Get all jobs
*DevicesApi* | [**reserveDevice**](docs/DevicesApi.md#reserveDevice) | **POST** /api/devices/reserve | Reserves a device
*DevicesApi* | [**reserveDevice1**](docs/DevicesApi.md#reserveDevice1) | **GET** /api/devices/{devid} | Gets device information
*FileSystemApi* | [**download**](docs/FileSystemApi.md#download) | **GET** /api/devices/{devid}/filesystem/content | Downloads a file
*FileSystemApi* | [**listFiles**](docs/FileSystemApi.md#listFiles) | **GET** /api/devices/{devid}/filesystem/list | Lists files
*FileSystemApi* | [**upload**](docs/FileSystemApi.md#upload) | **POST** /api/devices/{devid}/filesystem/content | Uploads a file
*InputApi* | [**broadcast**](docs/InputApi.md#broadcast) | **POST** /api/devices/{devid}/input/android/broadcast | Sends an intent to the system to start a service
*InputApi* | [**startActivity**](docs/InputApi.md#startActivity) | **POST** /api/devices/{devid}/input/android/startActivity | Sends an intent to the system to start an activity
*InputApi* | [**startForegroundService**](docs/InputApi.md#startForegroundService) | **POST** /api/devices/{devid}/input/android/startForegroundService | Sends an intent to the system to start a service
*InputApi* | [**startService**](docs/InputApi.md#startService) | **POST** /api/devices/{devid}/input/android/startService | Sends an intent to the system to start a service
*InputApi* | [**stopService**](docs/InputApi.md#stopService) | **POST** /api/devices/{devid}/input/android/stopService | Sends an intent to the system to stop a service
*InputApi* | [**tap**](docs/InputApi.md#tap) | **GET** /api/devices/{devid}/input/tap | Taps on screen
*InputApi* | [**typeText**](docs/InputApi.md#typeText) | **GET** /api/devices/{devid}/input/typeText | Inputs text

## Documentation for Models

 - [AbstractApp](docs/AbstractApp.md)
 - [AccessDeniedException](docs/AccessDeniedException.md)
 - [AndroidApp](docs/AndroidApp.md)
 - [AndroidDeviceInformation](docs/AndroidDeviceInformation.md)
 - [AndroidDeviceRequirements](docs/AndroidDeviceRequirements.md)
 - [AndroidPermission](docs/AndroidPermission.md)
 - [DeviceInformation](docs/DeviceInformation.md)
 - [DeviceNotFoundException](docs/DeviceNotFoundException.md)
 - [DeviceRequirements](docs/DeviceRequirements.md)
 - [DeviceReservationFailedException](docs/DeviceReservationFailedException.md)
 - [FileOnDevice](docs/FileOnDevice.md)
 - [Intent](docs/Intent.md)
 - [ModelAPIException](docs/ModelAPIException.md)
 - [SavedEnvironment](docs/SavedEnvironment.md)
 - [UploadedFile](docs/UploadedFile.md)

## Documentation for Authorization

Authentication schemes defined for the API:
### APIKey

- **Type**: API key
- **API key parameter name**: APIKey
- **Location**: HTTP header


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author

helpdesk@codeinspect.de
