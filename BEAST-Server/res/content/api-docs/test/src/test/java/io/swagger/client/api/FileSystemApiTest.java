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

package io.swagger.client.api;

import io.swagger.client.ApiException;
import io.swagger.client.model.AccessDeniedException;
import java.io.File;
import io.swagger.client.model.FileOnDevice;
import io.swagger.client.model.ModelAPIException;
import org.junit.Test;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for FileSystemApi
 */
@Ignore
public class FileSystemApiTest {

    private final FileSystemApi api = new FileSystemApi();

    /**
     * Downloads a file
     *
     * Downloads a file
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void downloadTest() throws ApiException {
        Integer devid = null;
        String path = null;
        File response = api.download(devid, path);

        // TODO: test validations
    }
    /**
     * Lists files
     *
     * List files
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void listFilesTest() throws ApiException {
        Integer devid = null;
        String path = null;
        List<FileOnDevice> response = api.listFiles(devid, path);

        // TODO: test validations
    }
    /**
     * Uploads a file
     *
     * Uploads a file
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void uploadTest() throws ApiException {
        String path = null;
        Integer devid = null;
        File file = null;
        api.upload(path, devid, file);

        // TODO: test validations
    }
}