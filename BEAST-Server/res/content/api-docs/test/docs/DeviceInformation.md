# DeviceInformation

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ID** | **Integer** | The id of the device | 
**state** | [**StateEnum**](#StateEnum) | The state of the device | 
**batteryLevel** | **Integer** | The battery level (from 0 to 100 percent or -1 if unknown) | 
**reservedBy** | **String** | Who is using the device at the moment |  [optional]
**type** | **String** | Type | 

<a name="StateEnum"></a>
## Enum: StateEnum
Name | Value
---- | -----
OCCUPIED | &quot;OCCUPIED&quot;
FREE | &quot;FREE&quot;
ERROR | &quot;ERROR&quot;
PREPARING | &quot;PREPARING&quot;
DISCONNECTED | &quot;DISCONNECTED&quot;
