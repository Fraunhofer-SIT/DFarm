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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Android Intent
 */
@Schema(description = "An Android Intent")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2018-12-21T16:25:16.578+01:00[Europe/Berlin]")public class Intent {

  @SerializedName("action")
  private String action = null;

  @SerializedName("category")
  private String category = null;

  @SerializedName("extras")
  private Map<String, String> extras = null;

  @SerializedName("component")
  private String component = null;

  @SerializedName("dataURI")
  private String dataURI = null;

  @SerializedName("mimeType")
  private String mimeType = null;

  @SerializedName("flags")
  private Integer flags = null;
  public Intent action(String action) {
    this.action = action;
    return this;
  }

  

  /**
  * The action
  * @return action
  **/
  @Schema(example = "android.intent.action.MAIN", required = true, description = "The action")
  public String getAction() {
    return action;
  }
  public void setAction(String action) {
    this.action = action;
  }
  public Intent category(String category) {
    this.category = category;
    return this;
  }

  

  /**
  * The category
  * @return category
  **/
  @Schema(example = "android.intent.category.LAUNCHER", description = "The category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public Intent extras(Map<String, String> extras) {
    this.extras = extras;
    return this;
  }

  
  public Intent putExtrasItem(String key, String extrasItem) {
    if (this.extras == null) {
      this.extras = null;
    }
    this.extras.put(key, extrasItem);
    return this;
  }
  /**
  * The extras
  * @return extras
  **/
  @Schema(description = "The extras")
  public Map<String, String> getExtras() {
    return extras;
  }
  public void setExtras(Map<String, String> extras) {
    this.extras = extras;
  }
  public Intent component(String component) {
    this.component = component;
    return this;
  }

  

  /**
  * The component
  * @return component
  **/
  @Schema(description = "The component")
  public String getComponent() {
    return component;
  }
  public void setComponent(String component) {
    this.component = component;
  }
  public Intent dataURI(String dataURI) {
    this.dataURI = dataURI;
    return this;
  }

  

  /**
  * The data uri
  * @return dataURI
  **/
  @Schema(description = "The data uri")
  public String getDataURI() {
    return dataURI;
  }
  public void setDataURI(String dataURI) {
    this.dataURI = dataURI;
  }
  public Intent mimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  

  /**
  * The mime type
  * @return mimeType
  **/
  @Schema(description = "The mime type")
  public String getMimeType() {
    return mimeType;
  }
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
  public Intent flags(Integer flags) {
    this.flags = flags;
    return this;
  }

  

  /**
  * Flags
  * @return flags
  **/
  @Schema(example = "0", description = "Flags")
  public Integer getFlags() {
    return flags;
  }
  public void setFlags(Integer flags) {
    this.flags = flags;
  }
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Intent intent = (Intent) o;
    return Objects.equals(this.action, intent.action) &&
        Objects.equals(this.category, intent.category) &&
        Objects.equals(this.extras, intent.extras) &&
        Objects.equals(this.component, intent.component) &&
        Objects.equals(this.dataURI, intent.dataURI) &&
        Objects.equals(this.mimeType, intent.mimeType) &&
        Objects.equals(this.flags, intent.flags);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(action, category, extras, component, dataURI, mimeType, flags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Intent {\n");
    
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    extras: ").append(toIndentedString(extras)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    dataURI: ").append(toIndentedString(dataURI)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
    sb.append("    flags: ").append(toIndentedString(flags)).append("\n");
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
