package com.pfizer.plugin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
*
* Project NTLMAuth Cordova Plugin
* Created by Bao Thien Pham on 5/3/16.
* Copyright (c) 2016 Pfizer. All rights reserved.
*
*/

public class HttpManager{


    @IntDef({HTTP_EXECUTING, NOT_AUTHENTICATED, HTTP_SUCCESS,
            CONNECTIVITY_ERROR, OTHER_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpState {}

    @HttpManager.HttpState
    public static final int HTTP_EXECUTING = 0;
    @HttpManager.HttpState
    public static final int NOT_AUTHENTICATED = 1;
    @HttpManager.HttpState
    public static final int HTTP_SUCCESS = 2;
    @HttpManager.HttpState
    public static final int CONNECTIVITY_ERROR = 4;
    @HttpManager.HttpState
    public static final int OTHER_ERROR = 5;


    @HttpManager.HttpState
    private static int currentHttpState = HTTP_EXECUTING;

    /**
     * Represents the {@code Handler} that is used to update the state and notify observers on the
     * UI thread.
     */
    private static DataHandler dataHandler = new DataHandler();


    public interface HttpListener {
        void httpExecuting();
        void notAuthenticated();
        void httpSuccess(@Nullable String result);
        void connectivityError();
        void otherError();
    }

    private static HttpListener sHttpListener = null;

    /**
     * Keeps track from the plugin state
     * 
     * @param listener Represents the state of the plugin
     *        result   Result of the request
     */
    public static void setCurrentState(@NonNull HttpListener listener, @Nullable String result) {
        switch (HttpManager.currentHttpState) {
            case HttpManager.HTTP_EXECUTING:
                listener.httpExecuting();
                break;
            case HttpManager.NOT_AUTHENTICATED:
                listener.notAuthenticated();
                break;
            case HttpManager.HTTP_SUCCESS:
                listener.httpSuccess(result);
                break;
            case HttpManager.CONNECTIVITY_ERROR:
                listener.connectivityError();
                break;
            default:
                listener.otherError();
                break;
            case OTHER_ERROR:
                break;
        }
    }

    /**
     * Send and execute the request
     *
     * @param urlWithPar    URL with parameters
     *        methodName    HTTP Methods name
     *        headers       Headers values
     *        data          JSON format for PUT and POST method
     *        userName      For authentication
     *        password      For authentication
     */
    public static String retrieveData(@NonNull String urlWithPar,
                                      @NtlmMethods.HttpMethod @NonNull String methodName,
                                      @Nullable  Map<String, String> headers,
                                      @Nullable  Map<String, String> data,
                                      @NonNull String userName,
                                      @NonNull String password) {
        int resultState = 0;
        //switch to background thread
        RetrieveData retrieveData = new RetrieveData(urlWithPar, methodName, headers, data, userName, password);

        try {
            resultState = retrieveData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e("HttpManager", "Cannot execute task", e);
            dataHandler.updateState(CONNECTIVITY_ERROR);
            e.printStackTrace();
        }

        Log.i("HttpManager", "Authentication State: " + resultState);
        return retrieveData.getResultString();
    }

    /**
     * Used to retrieve data from the server in the background thread
     */
    private static class RetrieveData extends AsyncTask<Void, Void, Integer>{

        private String urlWithPar = "";
        private String methodName = "";
        private Map<String, String> headers;
        private Map<String, String> data;
        private String userName;
        private String password;

        private String resultString;

        // ------------------------------------------------------------
        // Constructors
        // ------------------------------------------------------------
        public RetrieveData(@NonNull String urlWithPar,
                            @NtlmMethods.HttpMethod @NonNull String methodName,
                            Map<String, String> headers,
                            Map<String, String> data,
                            @NonNull String userName,
                            @NonNull String password){

            this.urlWithPar = urlWithPar;
            this.methodName = methodName;
            this.headers    = headers;
            this.data       = data;
            this.userName   = userName;
            this.password   = password;
        }

        /**
         * Before Execution
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataHandler.updateState(HTTP_EXECUTING);
        }

        /**
         * Creates credentials and prepares request
         *
         * @return Integer Represents the plugin state
         *
         */
        @Override
        protected Integer doInBackground(Void... parameter) {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            //Authentication
            httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());

            httpClient.getCredentialsProvider().setCredentials(
                    // Limit the credentials only to the specified domain and port
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    // Specify credentials for ntlm, most of the time only user/pass is needed
                    new NTCredentials(userName, password, "", ""));

            HttpUriRequest request;

            switch (methodName){
                case NtlmMethods.HTTP_GET:
                    request = new HttpGet(urlWithPar);
                    break;
                case NtlmMethods.HTTP_POST:
                    try {
                        return returnData(httpClient, JSONtoStringEntity(urlWithPar, methodName));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("HttpManager", "Failed to create HttpUriRequest", e);
                        return OTHER_ERROR;
                    }
                case NtlmMethods.HTTP_PUT:
                    try {
                        return returnData(httpClient, JSONtoStringEntity(urlWithPar, methodName));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("HttpManager", "Failed to create HttpUriRequest", e);
                        return OTHER_ERROR;
                    }
                case NtlmMethods.HTTP_HEAD:
                    request = new HttpHead(urlWithPar);
                    break;
                case NtlmMethods.HTTP_DELETE:
                    request = new HttpDelete(urlWithPar);
                    break;
                default:
                    Log.e("HttpManager", "Invalid HTTP method name");
                    return OTHER_ERROR;
            }

            //headers for json data
            request.addHeader("Accept", "application/json");
            request.addHeader("Content-type", "application/json");

            //add headers from user
            if(headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }

            return returnData(httpClient, request);
        }

        /**
         * Update plugin state after executions
         * 
         * @param result    Represent plugin state
         */
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            switch (result) {
                case NOT_AUTHENTICATED:
                    dataHandler.updateState(NOT_AUTHENTICATED);
                    break;
                case CONNECTIVITY_ERROR:
                    dataHandler.updateState(CONNECTIVITY_ERROR);
                    break;
                default:
                    dataHandler.updateState(OTHER_ERROR);
                    break;
            }
        }

        /**
         * Get result 
         *
         * @return String 
         */
        public String getResultString(){
            return resultString;
        }

        /**
         * Prepares requrest for PUT and POST only.
         * 
         * @param urlWithPar URL with Paramaters
         *        methodName HTTP Method name
         *
         * @return HttpUriRequest Request for PUT and POST
         */
        private HttpUriRequest JSONtoStringEntity(String urlWithPar, String methodName) throws UnsupportedEncodingException {
            JSONObject json = new JSONObject(data);
            StringEntity stringEntity;

            switch (methodName){
                case NtlmMethods.HTTP_POST:
                    HttpPost requestPost = new HttpPost(urlWithPar);
                    stringEntity = new StringEntity(json.toString());
                    requestPost.setEntity(stringEntity);
                    requestPost.addHeader("Accept", "application/json");
                    requestPost.addHeader("Content-type", "application/json");
                    return requestPost;
                case NtlmMethods.HTTP_PUT:
                    HttpPut requestPut = new HttpPut(urlWithPar);
                    stringEntity = new StringEntity(json.toString());
                    requestPut.setEntity(stringEntity);
                    requestPut.addHeader("Accept", "application/json");
                    requestPut.addHeader("Content-type", "application/json");
                    return requestPut;
                default:
                    Log.e("HttpManager", "Method name does not match with either POST or PUT");
                    return null;
            }
        }

        /**
         * Checks the response and returns the result
         * 
         * @param httpClient    Represents the client
         *        request       Request from the client
         *
         * @return Integer 	    Represents the plugin state
         */
        private Integer returnData (DefaultHttpClient httpClient, HttpUriRequest request){
            StringBuilder resultStringBuilder = new StringBuilder();
            HttpResponse response;
            try {
                response = httpClient.execute(request);

                StatusLine statusLine = response.getStatusLine();
                Log.i("HttpManager", "HTTP Status Code: " + statusLine.getStatusCode());

                //Status 201 for POST
                if (statusLine.getStatusCode() == HttpStatus.SC_CREATED || statusLine.getStatusCode() == HttpStatus.SC_OK) {

                    if (request.getMethod().equals(NtlmMethods.HTTP_HEAD)){
                        Header[] headers = response.getAllHeaders();
                        for(Header header: headers){
                            resultStringBuilder.append(header);
                            resultStringBuilder.append("\n");
                        }
                        dataHandler.updateState(HTTP_SUCCESS, resultStringBuilder.toString());
                        resultString = resultStringBuilder.toString();
                        return HTTP_SUCCESS;
                    }

                    InputStream inputStream = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String str;
                    while ((str = reader.readLine()) != null) {
                        resultStringBuilder.append(str);
                        resultStringBuilder.append("\n");
                    }
                    reader.close();

                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                }
            } catch (ClientProtocolException e) {
                Log.e("HttpManager", "Failed to execute httpClient", e);
                return CONNECTIVITY_ERROR;
            } catch (IOException e){
                Log.e("HttpManager", "Failed to read inputStream", e);
                return OTHER_ERROR;
            }

            dataHandler.updateState(HTTP_SUCCESS, resultStringBuilder.toString());
            resultString = resultStringBuilder.toString();
            return HTTP_SUCCESS;
        }
    }



    /**
     * A {@code Handler} used to update the state and notify observers on the UI thread.
     */
    private static final class DataHandler extends Handler {

        public DataHandler() { super(Looper.getMainLooper()); }

        /**
         * Called by {@code HttpManager} member components to enforce the state Retention
         * Policy, update the state, and notify observers.
         *
         * @param state Represents the new state.
         */
        public void updateState(@HttpState int state) {
            sendEmptyMessage(state);
        }


        /**
         * Called by {@code HttpManager} member components to enforce the state Retention
         * Policy, update the state, and notify observers.
         *
         * @param state Represents the new state.
         */
        public void updateState(@HttpState int state, @Nullable String result) {
            Message msg = new Message();
            msg.what = state;
            msg.obj = result;
            sendMessage(msg);
        }

         /**
         * Called by {@code HttpManager} member components to enforce the state Retention
         * Policy, update the state, and notify observers.
         *
         * @param state Represents the new state.
         */
        @Override
        public void handleMessage(Message msg) {
            currentHttpState = msg.what;

            if (sHttpListener != null) {
                if (msg.obj instanceof String) {
                    setCurrentState(sHttpListener, (String) msg.obj);
                } else {
                    setCurrentState(sHttpListener, null);
                }
            }
        }
    }
}
