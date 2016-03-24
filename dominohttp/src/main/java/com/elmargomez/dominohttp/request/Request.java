/*
 * Copyright 2016 Elmar Rhex Gomez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elmargomez.dominohttp.request;

import com.elmargomez.dominohttp.ContentType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Request<T> {

    public static final int EXECUTION_REQUEST_SUCCESS = 0;
    public static final int EXECUTION_REQUEST_ERROR = 1;
    public static final int EXECUTION_FAILURE_ON_DEPLOY = 2;


    private ArrayList<Request> dependent;
    private HashMap<String, String> header;

    // the listener for our request.
    private OnInternalFailedListener internalFailedListener;
    private OnRequestFailedListener requestFailedListener;

    private int retryCount;
    private String errorMessage = null;
    private String contentType = null;
    private String method = null;
    private String stringURL = null;

    public Request() {
        this.dependent = new ArrayList<>();
        this.header = new HashMap<>();
        this.contentType = ContentType.APPLICATION_JSON;
        this.retryCount = -1;
    }

    public T setContentType(String string) {
        this.contentType = string;
        return (T) this;
    }

    public String getContentType() {
        return contentType;
    }

    public T setMethod(String method) {
        this.method = method;
        return (T) this;
    }

    public T setRetryCount(int c) {
        this.retryCount = c;
        return (T) this;
    }

    public void decrimentRetryLeft() {
        if (retryCount == -1)
            return;

        retryCount--;
    }

    public boolean canRetry() {
        if (retryCount == -1)
            return true;
        return (retryCount != 0);
    }

    public T setURL(String url) {
        this.stringURL = url;
        return (T) this;
    }

    public T addDependant(Request dependentR) {
        dependent.add(dependentR);
        return (T) this;
    }

    public T addHeader(String key, String val) {
        header.put(key, val);
        return (T) this;
    }

    public ArrayList<Request> getDependentRequests() {
        return dependent;
    }

    public void setOnInternalFailedListener(OnInternalFailedListener f) {
        this.internalFailedListener = f;
    }

    public OnInternalFailedListener getInternalFailedListener() {
        return internalFailedListener;
    }

    public void setOnRequestFailedListener(OnRequestFailedListener f) {
        this.requestFailedListener = f;
    }

    public OnRequestFailedListener getRequestFailedListener() {
        return requestFailedListener;
    }

    public HttpURLConnection getConnection() throws IOException {
        if (stringURL == null)
            throw new NullPointerException("URL is null.");

        if (method == null)
            throw new NullPointerException("Method is null, please add method e.g POST.");

        if (contentType == null)
            throw new NullPointerException("Content Type is null, please add Content-Type " +
                    "e.g. application/json");

        URL url = new URL(stringURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", contentType);
        Set<Map.Entry<String, String>> entries = header.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        return connection;
    }

    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage(){
        if(errorMessage == null)
            return "";

        return errorMessage;
    }

    public abstract int executed();

    /**
     * Request Success Listener
     *
     * @param <T>
     */
    interface OnSuccessListener<T> {

        void response(T t);

    }

    /**
     * A Failure Listener
     */
    interface OnInternalFailedListener {

        void response(String error);

    }

    interface OnRequestFailedListener {

        void response(String response, int statusCode);

    }

}
