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
 * Access denied
 */
@Schema(description = "Access denied")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2018-12-21T16:25:16.578+01:00[Europe/Berlin]")public class AccessDeniedException {

  @SerializedName("title")
  private String title = null;

  @SerializedName("description")
  private String description = null;

  @SerializedName("code")
  private Integer code = null;
  public AccessDeniedException title(String title) {
    this.title = title;
    return this;
  }

  

  /**
  * A short description of the error that occured
  * @return title
  **/
  @Schema(example = "Internal Error", required = true, description = "A short description of the error that occured")
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public AccessDeniedException description(String description) {
    this.description = description;
    return this;
  }

  

  /**
  * A more detailed description of the error that occured
  * @return description
  **/
  @Schema(example = "Out of storage space", required = true, description = "A more detailed description of the error that occured")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public AccessDeniedException code(Integer code) {
    this.code = code;
    return this;
  }

  

  /**
  * Get code
  * @return code
  **/
  @Schema(description = "")
  public Integer getCode() {
    return code;
  }
  public void setCode(Integer code) {
    this.code = code;
  }
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessDeniedException accessDeniedException = (AccessDeniedException) o;
    return Objects.equals(this.title, accessDeniedException.title) &&
        Objects.equals(this.description, accessDeniedException.description) &&
        Objects.equals(this.code, accessDeniedException.code);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(title, description, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessDeniedException {\n");
    
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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
