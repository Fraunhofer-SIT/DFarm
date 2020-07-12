# DevicesApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getDevices**](DevicesApi.md#getDevices) | **POST** /api/devices | Returns a list of devices
[**getScreenshot**](DevicesApi.md#getScreenshot) | **GET** /api/devices/{devid}/screenshot | Downloads a screenshot
[**releaseAllDevices**](DevicesApi.md#releaseAllDevices) | **GET** /api/devices/releaseAll | Release all devices reserved by the API key
[**releaseDevice**](DevicesApi.md#releaseDevice) | **GET** /api/devices/{devid}/release | Get all jobs
[**reserveDevice**](DevicesApi.md#reserveDevice) | **GET** /api/devices/{devid} | Gets device information
[**reserveDevice1**](DevicesApi.md#reserveDevice1) | **POST** /api/devices/reserve | Reserves a device

<a name="getDevices"></a>
# **getDevices**
> List&lt;DeviceInformation&gt; getDevices(body)

Returns a list of devices

Returns devices

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
DeviceRequirements body = new DeviceRequirements(); // DeviceRequirements | 
try {
    List<DeviceInformation> result = apiInstance.getDevices(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#getDevices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**DeviceRequirements**](DeviceRequirements.md)|  |

### Return type

[**List&lt;DeviceInformation&gt;**](DeviceInformation.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getScreenshot"></a>
# **getScreenshot**
> Object getScreenshot(devid)

Downloads a screenshot

Downloads a screenshot

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
Integer devid = 56; // Integer | The id of device
try {
    Object result = apiInstance.getScreenshot(devid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#getScreenshot");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |

### Return type

**Object**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream, image/png

<a name="releaseAllDevices"></a>
# **releaseAllDevices**
> releaseAllDevices()

Release all devices reserved by the API key

Release all devices reserved by the API key

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
try {
    apiInstance.releaseAllDevices();
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#releaseAllDevices");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="releaseDevice"></a>
# **releaseDevice**
> releaseDevice(devid)

Get all jobs

Get all jobs currently on the analysis server. Note that for performance reasons, this method does not return the job results. Instead, it returns the job status and metadata.

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
Integer devid = 56; // Integer | The device id
try {
    apiInstance.releaseDevice(devid);
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#releaseDevice");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The device id |

### Return type

null (empty response body)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="reserveDevice"></a>
# **reserveDevice**
> DeviceInformation reserveDevice(devid)

Gets device information

Gets device information

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
Integer devid = 56; // Integer | The device id
try {
    DeviceInformation result = apiInstance.reserveDevice(devid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#reserveDevice");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The device id |

### Return type

[**DeviceInformation**](DeviceInformation.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="reserveDevice1"></a>
# **reserveDevice1**
> DeviceInformation reserveDevice1(body)

Reserves a device

Reserves a device and returns it (if successful)

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DevicesApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DevicesApi apiInstance = new DevicesApi();
DeviceRequirements body = new DeviceRequirements(); // DeviceRequirements | Device requirements
try {
    DeviceInformation result = apiInstance.reserveDevice1(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DevicesApi#reserveDevice1");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**DeviceRequirements**](DeviceRequirements.md)| Device requirements | [optional]

### Return type

[**DeviceInformation**](DeviceInformation.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

