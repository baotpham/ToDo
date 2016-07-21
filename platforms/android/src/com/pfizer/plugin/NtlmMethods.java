package com.pfizer.plugin;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
*
* Project NTLMAuth Cordova Plugin
* Created by Bao Thien Pham on 5/3/16.
* Copyright (c) 2016 Pfizer. All rights reserved.
*
*/

public class NtlmMethods {

    @StringDef({HTTP_GET, HTTP_POST, HTTP_PUT, HTTP_DELETE, HTTP_HEAD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpMethod {}

    @HttpMethod
    public static final String HTTP_GET     = "GET";
    @HttpMethod
    public static final String HTTP_POST    = "POST";
    @HttpMethod
    public static final String HTTP_PUT     = "PUT";
    @HttpMethod
    public static final String HTTP_HEAD    = "HEAD";
    @HttpMethod
    public static final String HTTP_DELETE  = "DELETE";

    private String username;
    private String password;
    
    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------
    public NtlmMethods(@NonNull String username, @NonNull String password){
        this.username = username;
        this.password = password;
    }

    /**
     * GET method
     *
     * @param url               URL 
     *        path              Parameters for url 
     *        headers           Request headers
     *
     * @return String           Result from the request
     */
    public String NtlmGET(@NonNull String url, @Nullable String path, @Nullable  Map<String, String> headers){
        String urlWithPar = url + path;
        return HttpManager.retrieveData(urlWithPar, HTTP_GET, headers, null, username, password);
    }

    /**
     * POST method
     *
     * @param url               URL 
     *        path              Parameters for url 
     *        headers           Request headers
     *        data              
     *
     * @return String           Result from the request
     */
    public String NtlmPOST (@NonNull String url, @Nullable String path, @Nullable  Map<String, String> headers, @NonNull Map<String, String> data){
        String urlWithPar = url + path;
        return HttpManager.retrieveData(urlWithPar, HTTP_POST, headers, data, username, password);
    }

    /**
     * PUT method
     *
     * @param url               URL 
     *        path              Parameters for url 
     *        headers           Request headers
     *        data              
     *
     * @return String           Result from the request
     */
    public String NtlmPUT (@NonNull String url, @Nullable String path, @Nullable  Map<String, String> headers, @NonNull Map<String, String> data){
        String urlWithPar = url + path;
        return HttpManager.retrieveData(urlWithPar, HTTP_PUT, headers, data, username, password);
    }

    /**
     * HEAD method
     *
     * @param url               URL 
     *        path              Parameters for url 
     *        headers           Request headers
     *
     * @return String           Result from the request
     */
    public String NtlmHEAD (@NonNull String url, @Nullable String path, @Nullable  Map<String, String> headers){
        String urlWithPar = url + path;
        return HttpManager.retrieveData(urlWithPar, HTTP_HEAD, headers, null, username, password);
    }

    /**
     * DELETE method
     *
     * @param url               URL 
     *        path              Parameters for url 
     *        headers           Request headers
     *
     * @return String           Result from the request
     */
    public String NtlmDELETE (@NonNull String url, @Nullable String path, @Nullable  Map<String, String> headers){
        String urlWithPar = url + path;
        return HttpManager.retrieveData(urlWithPar, HTTP_DELETE, headers, null, username, password);
    }
}
