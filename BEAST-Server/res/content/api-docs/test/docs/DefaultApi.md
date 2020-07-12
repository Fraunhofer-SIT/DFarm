# DefaultApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getInstalledAppInfo**](DefaultApi.md#getInstalledAppInfo) | **GET** /Foo | Test

<a name="getInstalledAppInfo"></a>
# **getInstalledAppInfo**
> BaseClass getInstalledAppInfo()

Test

Test

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
try {
    BaseClass result = apiInstance.getInstalledAppInfo();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getInstalledAppInfo");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**BaseClass**](BaseClass.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

