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
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;

/**
 * SavedEnvironment
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2018-12-21T16:25:16.578+01:00[Europe/Berlin]")public class SavedEnvironment {

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("device")
  private Integer device = null;

  @SerializedName("user")
  private String user = null;
  public SavedEnvironment id(Integer id) {
    this.id = id;
    return this;
  }

  

  /**
  * Get id
  * @return id
  **/
  @Schema(description = "")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public SavedEnvironment name(String name) {
    this.name = name;
    return this;
  }

  

  /**
  * Get name
  * @return name
  **/
  @Schema(description = "")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public SavedEnvironment device(Integer device) {
    this.device = device;
    return this;
  }

  

  /**
  * Get device
  * @return device
  **/
  @Schema(description = "")
  public Integer getDevice() {
    return device;
  }
  public void setDevice(Integer device) {
    this.device = device;
  }
  public SavedEnvironment user(String user) {
    this.user = user;
    return this;
  }

  

  /**
  * Get user
  * @return user
  **/
  @Schema(description = "")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SavedEnvironment savedEnvironment = (SavedEnvironment) o;
    return Objects.equals(this.id, savedEnvironment.id) &&
        Objects.equals(this.name, savedEnvironment.name) &&
        Objects.equals(this.device, savedEnvironment.device) &&
        Objects.equals(this.user, savedEnvironment.user);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, name, device, user);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SavedEnvironment {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    device: ").append(toIndentedString(device)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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
