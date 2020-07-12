# AppsApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getInstalledAppInfo**](AppsApi.md#getInstalledAppInfo) | **GET** /api/devices/{devid}/apps/{appid} | Shows information about a specific app
[**getInstalledApps**](AppsApi.md#getInstalledApps) | **GET** /api/devices/{devid}/apps | Lists installed apps
[**installApplication**](AppsApi.md#installApplication) | **POST** /api/devices/{devid}/apps/installApplication | Installs an app
[**uninstallApplication**](AppsApi.md#uninstallApplication) | **DELETE** /api/devices/{devid}/apps/{appid} | Uninstalls an app

<a name="getInstalledAppInfo"></a>
# **getInstalledAppInfo**
> AbstractApp getInstalledAppInfo(devid, appid)

Shows information about a specific app

Shows information about a specific app

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AppsApi;

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
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **appid** | **String**| The id of app |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getInstalledApps"></a>
# **getInstalledApps**
> List&lt;AbstractApp&gt; getInstalledApps(devid)

Lists installed apps

Lists installed apps

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AppsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

AppsApi apiInstance = new AppsApi();
Integer devid = 56; // Integer | The id of device
try {
    List<AbstractApp> result = apiInstance.getInstalledApps(devid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#getInstalledApps");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |

### Return type

[**List&lt;AbstractApp&gt;**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="installApplication"></a>
# **installApplication**
> AbstractApp installApplication(devid, file)

Installs an app

Uploads a file and installs it as an app

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AppsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

AppsApi apiInstance = new AppsApi();
Integer devid = 56; // Integer | The id of device
File file = new File("file_example"); // File | 
try {
    AbstractApp result = apiInstance.installApplication(devid, file);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#installApplication");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **file** | **File**|  | [optional]

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

<a name="uninstallApplication"></a>
# **uninstallApplication**
> AbstractApp uninstallApplication(devid, appid, file)

Uninstalls an app

Uninstalls an app

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.AppsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

AppsApi apiInstance = new AppsApi();
Integer devid = 56; // Integer | The id of device
String appid = "appid_example"; // String | The id of app
File file = new File("file_example"); // File | 
try {
    AbstractApp result = apiInstance.uninstallApplication(devid, appid, file);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#uninstallApplication");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **appid** | **String**| The id of app |
 **file** | **File**|  | [optional]

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

