# DeviceRequirements

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**minBatteryLevel** | **Integer** | The minimum battery level (-1 if don&#x27;t care) | 
**state** | [**StateEnum**](#StateEnum) | A required state |  [optional]
**type** | **String** | Type | 
**reservedBy** | **String** | The device must be reserved by the given user |  [optional]

<a name="StateEnum"></a>
## Enum: StateEnum
Name | Value
---- | -----
OCCUPIED | &quot;OCCUPIED&quot;
FREE | &quot;FREE&quot;
ERROR | &quot;ERROR&quot;
PREPARING | &quot;PREPARING&quot;
DISCONNECTED | &quot;DISCONNECTED&quot;
