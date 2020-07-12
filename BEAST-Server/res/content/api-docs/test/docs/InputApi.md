# InputApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**broadcast**](InputApi.md#broadcast) | **POST** /api/devices/{devid}/input/android/broadcast | Sends an intent to the system to start a service
[**startActivity**](InputApi.md#startActivity) | **POST** /api/devices/{devid}/input/android/startActivity | Sends an intent to the system to start an activity
[**startForegroundService**](InputApi.md#startForegroundService) | **POST** /api/devices/{devid}/input/android/startForegroundService | Sends an intent to the system to start a service
[**startService**](InputApi.md#startService) | **POST** /api/devices/{devid}/input/android/startService | Sends an intent to the system to start a service
[**stopService**](InputApi.md#stopService) | **POST** /api/devices/{devid}/input/android/stopService | Sends an intent to the system to stop a service
[**tap**](InputApi.md#tap) | **GET** /api/devices/{devid}/input/tap | Taps on screen
[**typeText**](InputApi.md#typeText) | **GET** /api/devices/{devid}/input/typeText | Inputs text

<a name="broadcast"></a>
# **broadcast**
> List&lt;String&gt; broadcast(devid, receiverPermission, body)

Sends an intent to the system to start a service

Sends an intent to the system to start a service. Android only

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
String receiverPermission = "receiverPermission_example"; // String | The permission a receiver needs to have
Intent body = new Intent(); // Intent | The intent ot send
try {
    List<String> result = apiInstance.broadcast(devid, receiverPermission, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#broadcast");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **receiverPermission** | **String**| The permission a receiver needs to have |
 **body** | [**Intent**](Intent.md)| The intent ot send | [optional]

### Return type

**List&lt;String&gt;**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="startActivity"></a>
# **startActivity**
> List&lt;String&gt; startActivity(devid, forceStopBefore, waitForDebugger, body)

Sends an intent to the system to start an activity

Sends an intent to the system to start an activity. Android only

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
Boolean forceStopBefore = true; // Boolean | Whether to force stop the application before
Boolean waitForDebugger = true; // Boolean | Whether to wait for a debugger
Intent body = new Intent(); // Intent | The intent ot send
try {
    List<String> result = apiInstance.startActivity(devid, forceStopBefore, waitForDebugger, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#startActivity");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **forceStopBefore** | **Boolean**| Whether to force stop the application before |
 **waitForDebugger** | **Boolean**| Whether to wait for a debugger |
 **body** | [**Intent**](Intent.md)| The intent ot send | [optional]

### Return type

**List&lt;String&gt;**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="startForegroundService"></a>
# **startForegroundService**
> List&lt;String&gt; startForegroundService(devid, body)

Sends an intent to the system to start a service

Sends an intent to the system to start a service. Android only

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
Intent body = new Intent(); // Intent | The intent ot send
try {
    List<String> result = apiInstance.startForegroundService(devid, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#startForegroundService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **body** | [**Intent**](Intent.md)| The intent ot send | [optional]

### Return type

**List&lt;String&gt;**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="startService"></a>
# **startService**
> List&lt;String&gt; startService(devid, body)

Sends an intent to the system to start a service

Sends an intent to the system to start a service. Android only

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
Intent body = new Intent(); // Intent | The intent ot send
try {
    List<String> result = apiInstance.startService(devid, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#startService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **body** | [**Intent**](Intent.md)| The intent ot send | [optional]

### Return type

**List&lt;String&gt;**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="stopService"></a>
# **stopService**
> List&lt;String&gt; stopService(devid, body)

Sends an intent to the system to stop a service

Sends an intent to the system to stop a service. Android only

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
Intent body = new Intent(); // Intent | The intent ot send
try {
    List<String> result = apiInstance.stopService(devid, body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#stopService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **body** | [**Intent**](Intent.md)| The intent ot send | [optional]

### Return type

**List&lt;String&gt;**

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="tap"></a>
# **tap**
> AbstractApp tap(devid, x, y)

Taps on screen

Taps on screen

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
Integer x = 56; // Integer | The x coordinate
Integer y = 56; // Integer | The y coordinate
try {
    AbstractApp result = apiInstance.tap(devid, x, y);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#tap");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **x** | **Integer**| The x coordinate |
 **y** | **Integer**| The y coordinate |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="typeText"></a>
# **typeText**
> AbstractApp typeText(devid, text)

Inputs text

Inputs text

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.InputApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

InputApi apiInstance = new InputApi();
Integer devid = 56; // Integer | The id of device
String text = "text_example"; // String | The text
try {
    AbstractApp result = apiInstance.typeText(devid, text);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling InputApi#typeText");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **text** | **String**| The text |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

