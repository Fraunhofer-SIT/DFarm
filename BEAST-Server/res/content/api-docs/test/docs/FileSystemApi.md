# FileSystemApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**download**](FileSystemApi.md#download) | **GET** /api/devices/{devid}/filesystem/content | Downloads a file
[**listFiles**](FileSystemApi.md#listFiles) | **GET** /api/devices/{devid}/filesystem/list | Lists files
[**upload**](FileSystemApi.md#upload) | **POST** /api/devices/{devid}/filesystem/content | Uploads a file

<a name="download"></a>
# **download**
> File download(devid, path)

Downloads a file

Downloads a file

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.FileSystemApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

FileSystemApi apiInstance = new FileSystemApi();
Integer devid = 56; // Integer | The id of device
String path = "path_example"; // String | The path
try {
    File result = apiInstance.download(devid, path);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling FileSystemApi#download");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **path** | **String**| The path |

### Return type

[**File**](File.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream

<a name="listFiles"></a>
# **listFiles**
> List&lt;FileOnDevice&gt; listFiles(devid, path)

Lists files

List files

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.FileSystemApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

FileSystemApi apiInstance = new FileSystemApi();
Integer devid = 56; // Integer | The id of device
String path = "path_example"; // String | Path
try {
    List<FileOnDevice> result = apiInstance.listFiles(devid, path);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling FileSystemApi#listFiles");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **devid** | **Integer**| The id of device |
 **path** | **String**| Path |

### Return type

[**List&lt;FileOnDevice&gt;**](FileOnDevice.md)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="upload"></a>
# **upload**
> upload(path, devid, file)

Uploads a file

Uploads a file

### Example
```java
// Import classes:
//import io.swagger.client.ApiClient;
//import io.swagger.client.ApiException;
//import io.swagger.client.Configuration;
//import io.swagger.client.auth.*;
//import io.swagger.client.api.FileSystemApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();

// Configure API key authorization: APIKey
ApiKeyAuth APIKey = (ApiKeyAuth) defaultClient.getAuthentication("APIKey");
APIKey.setApiKey("YOUR API KEY");
// Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
//APIKey.setApiKeyPrefix("Token");

FileSystemApi apiInstance = new FileSystemApi();
String path = "path_example"; // String | Path
Integer devid = 56; // Integer | The id of device
File file = new File("file_example"); // File | 
try {
    apiInstance.upload(path, devid, file);
} catch (ApiException e) {
    System.err.println("Exception when calling FileSystemApi#upload");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **path** | **String**| Path |
 **devid** | **Integer**| The id of device |
 **file** | **File**|  | [optional]

### Return type

null (empty response body)

### Authorization

[APIKey](../README.md#APIKey)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json

