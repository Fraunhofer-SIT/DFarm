/*
 * Private API
 * Assessment Private API - Do not use!
 *
 * OpenAPI spec version: 0.1
 * Contact: helpdesk@codeinspect.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.client.model.DeviceRequirements;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;

/**
 * Android device requirements
 */
@Schema(description = "Android device requirements")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2018-12-21T16:25:16.578+01:00[Europe/Berlin]")public class AndroidDeviceRequirements extends DeviceRequirements {

  @SerializedName("minSDKVersion")
  private Integer minSDKVersion = null;

  @SerializedName("maxSDKVersion")
  private Integer maxSDKVersion = null;
  public AndroidDeviceRequirements minSDKVersion(Integer minSDKVersion) {
    this.minSDKVersion = minSDKVersion;
    return this;
  }

  

  /**
  * The minimum SDK version (-1 if don&#x27;t care)
  * @return minSDKVersion
  **/
  @Schema(description = "The minimum SDK version (-1 if don't care)")
  public Integer getMinSDKVersion() {
    return minSDKVersion;
  }
  public void setMinSDKVersion(Integer minSDKVersion) {
    this.minSDKVersion = minSDKVersion;
  }
  public AndroidDeviceRequirements maxSDKVersion(Integer maxSDKVersion) {
    this.maxSDKVersion = maxSDKVersion;
    return this;
  }

  

  /**
  * The maximum SDK version (-1 if don&#x27;t care)
  * @return maxSDKVersion
  **/
  @Schema(description = "The maximum SDK version (-1 if don't care)")
  public Integer getMaxSDKVersion() {
    return maxSDKVersion;
  }
  public void setMaxSDKVersion(Integer maxSDKVersion) {
    this.maxSDKVersion = maxSDKVersion;
  }
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AndroidDeviceRequirements androidDeviceRequirements = (AndroidDeviceRequirements) o;
    return Objects.equals(this.minSDKVersion, androidDeviceRequirements.minSDKVersion) &&
        Objects.equals(this.maxSDKVersion, androidDeviceRequirements.maxSDKVersion) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(minSDKVersion, maxSDKVersion, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AndroidDeviceRequirements {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    minSDKVersion: ").append(toIndentedString(minSDKVersion)).append("\n");
    sb.append("    maxSDKVersion: ").append(toIndentedString(maxSDKVersion)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}