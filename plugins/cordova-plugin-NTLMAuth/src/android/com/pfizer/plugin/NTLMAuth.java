package com.pfizer.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

/**
*
* Project NTLMAuth Cordova Plugin
* Created by Bao Thien Pham on 5/3/16.
* Copyright (c) 2016 Pfizer. All rights reserved.
*
*/

public class NTLMAuth extends CordovaPlugin {

    private String result;

    /**
     * This method will be called from the Javascript file. This acts as a bridge from Cordova to Native Android
     *
     * @param action            Represents the method which will be called to execute a task
     *        args              An array of arguments from Javascript
     *        callbackContext   Javascript callback
     *
     * @return boolean  Whether or not the method is executed
     */
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("callNtlmMethods".equals(action)) {

            String userName                 = args.getString(0);
            String password                 = args.getString(1);
            String url                      = args.getString(2);
            String urlPath                  = args.getString(3);
            String methodName               = args.getString(4);
            String headers                  = args.getString(5);
            String data                     = args.getString(6);

            this.callNtlmMethods(userName, password, url, urlPath, methodName, headers, data, callbackContext);
            return true;
        }
        return false;
    }

     /**
     * Handles all method calls 
     *
     * @param username          For authentication 
     *        password          For authentication
     *        url               URL 
     *        urlPath           Parameters for url 
     *        methodName        HTTP method name
     *        headers           Request headers
     *        data              Data for PUT and POST
     *        callbackContext   Javascript callback
     */
    private void callNtlmMethods(@NonNull String userName,
                                         @NonNull String password,
                                         @NonNull String url,
                                         @NonNull String urlPath,
                                         @NonNull String methodName,
                                         @Nullable String headers,
                                         @Nullable String data,
                                         CallbackContext callbackContext) {

        NtlmMethods ntlmMethods = new NtlmMethods(userName, password);

        if (userName.equals("") || password.equals("")) {
            callbackContext.error("User Name and/or Password is empty");
        } else {
            if (url.equals("")) {
                callbackContext.error("URL is empty");
            } else if (methodName.equals("")) {
                callbackContext.error("Method Name is empty");
            } else {

                HashMap<String, String> headersConverted = convertToHashMap(headers);

                switch (methodName) {
                    case NtlmMethods.HTTP_GET:
                        result = ntlmMethods.NtlmGET(url, urlPath, headersConverted);
                        if (!result.equals("")) {
                            callbackContext.success("Successful Making GET Request");
                        }else{
                            callbackContext.error("Failed Making GET Request");
                        }
                        break;
                    case NtlmMethods.HTTP_POST:
                        if (data == null) {
                            callbackContext.error("Data is empty");
                            break;
                        } else {
                            HashMap<String, String> dataConverted = convertToHashMap(data);
                            assert dataConverted != null;
                            result = ntlmMethods.NtlmPOST(url, urlPath, headersConverted, dataConverted);
                            if (!result.equals("")) {
                                callbackContext.success("Successful Making POST Request");
                            }else{
                                callbackContext.error("Failed Making POST Request");
                            }
                            break;
                        }
                    case NtlmMethods.HTTP_PUT:
                        if (data == null) {
                            callbackContext.error("Data is empty");
                            break;
                        } else {
                            HashMap<String, String> dataConverted = convertToHashMap(data);
                            assert dataConverted != null;
                            result = ntlmMethods.NtlmPUT(url, urlPath, headersConverted, dataConverted);
                            if (!result.equals("")) {
                                callbackContext.success("Successful Making PUT Request");
                            }else{
                                callbackContext.error("Failed Making PUT Request");
                            }
                            break;
                        }
                    case NtlmMethods.HTTP_HEAD:
                        result = ntlmMethods.NtlmHEAD(url, urlPath, headersConverted);
                        if (!result.equals("")) {
                            callbackContext.success("Successful Making HEAD Request");
                        }else{
                            callbackContext.error("Failed Making HEAD Request");
                        }
                        break;
                    case NtlmMethods.HTTP_DELETE:
                        result = ntlmMethods.NtlmDELETE(url, urlPath, headersConverted);
                        if (!result.equals("")) {
                            callbackContext.success("Successful Making DELETE Request");
                        }else{
                            callbackContext.error("Failed Making DELETE Request");
                        }
                        break;
                    default:
                        callbackContext.error("methodName does not match");
                }
                Log.i("NTLMAuth", "Result: " + result);
            }
        }
    }
    
    /**
     * Convert String to HashMap for headers and data
     *
     * @param input          String values from Javascript 
     *
     * @return HashMap<String, String> 
     */
    private HashMap<String, String> convertToHashMap(String input){
        if (input.equals("")){
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        String[] inputArray = input.split(",");
        for (String eachInput : inputArray){
            String[] keysAndValues = eachInput.split(":");
            result.put(keysAndValues[0], keysAndValues[1]);
        }

        return result;
    }

    public String getResult(){
        return result;
    }
}
