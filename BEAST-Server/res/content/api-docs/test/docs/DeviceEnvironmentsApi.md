# DeviceEnvironmentsApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteState**](DeviceEnvironmentsApi.md#deleteState) | **DELETE** /api/devices/{devid}/environments/{saveName} | Deletes the current state as the new saved state
[**listStates**](DeviceEnvironmentsApi.md#listStates) | **GET** /api/devices/{devid}/environments | Returns a list of saved states
[**loadState**](DeviceEnvironmentsApi.md#loadState) | **GET** /api/devices/{devid}/environments/{saveName}/load | Loads the saved state
[**saveState**](DeviceEnvironmentsApi.md#saveState) | **PUT** /api/devices/{devid}/environments/{saveName} | Saves the current state as the new saved state

<a name="deleteState"></a>
# **deleteState**
> AbstractApp deleteState(devid, saveName)

Deletes the current state as the new saved state

Saves the state

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DeviceEnvironmentsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DeviceEnvironmentsApi apiInstance = new DeviceEnvironmentsApi();
Integer devid = 56; // Integer | The id of device
String saveName = "saveName_example"; // String | The id of device
try {
    AbstractApp result = apiInstance.deleteState(devid, saveName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceEnvironmentsApi#deleteState");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **saveName** | **String**| The id of device |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="listStates"></a>
# **listStates**
> List&lt;SavedEnvironment&gt; listStates(devid)

Returns a list of saved states

List of saved environment states

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DeviceEnvironmentsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DeviceEnvironmentsApi apiInstance = new DeviceEnvironmentsApi();
Integer devid = 56; // Integer | The id of device
try {
    List<SavedEnvironment> result = apiInstance.listStates(devid);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceEnvironmentsApi#listStates");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |

### Return type

[**List&lt;SavedEnvironment&gt;**](SavedEnvironment.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="loadState"></a>
# **loadState**
> AbstractApp loadState(devid, saveName)

Loads the saved state

Loads the saved state

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DeviceEnvironmentsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DeviceEnvironmentsApi apiInstance = new DeviceEnvironmentsApi();
Integer devid = 56; // Integer | The id of device
String saveName = "saveName_example"; // String | The id of device
try {
    AbstractApp result = apiInstance.loadState(devid, saveName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceEnvironmentsApi#loadState");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **saveName** | **String**| The id of device |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="saveState"></a>
# **saveState**
> AbstractApp saveState(devid, saveName)

Saves the current state as the new saved state

Saves the state

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.DeviceEnvironmentsApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

DeviceEnvironmentsApi apiInstance = new DeviceEnvironmentsApi();
Integer devid = 56; // Integer | The id of device
String saveName = "saveName_example"; // String | The id of device
try {
    AbstractApp result = apiInstance.saveState(devid, saveName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DeviceEnvironmentsApi#saveState");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **saveName** | **String**| The id of device |

### Return type

[**AbstractApp**](AbstractApp.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

