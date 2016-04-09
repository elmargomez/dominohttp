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
import com.elmargomez.dominohttp.listener.FailedListener;
import com.elmargomez.dominohttp.listener.OnExceptionListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Request implements Comparable<Request> {

    public static final int EXECUTION_REQUEST_SUCCESS = 0;
    public static final int EXECUTION_REQUEST_ERROR = 1;
    public static final int EXECUTION_FAILURE_ON_DEPLOY = 2;

    private ArrayList<Request> dependent;
    private HashMap<String, String> header;

    // the listener for our request.
    private OnExceptionListener internalFailedListener;
    private FailedListener requestFailedListener;

    private int retryCount;
    private String errorMessage = null;
    private String contentType = null;
    private String method = null;
    private String stringURL = null;
    private String debugKey = null;

    public Request() {
        this.dependent = new ArrayList<>();
        this.header = new HashMap<>();
        this.contentType = ContentType.APPLICATION_JSON;
        this.retryCount = -1;
    }

    public void setContentType(String string) {
        this.contentType = string;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRetryCount(int c) {
        this.retryCount = c;
    }

    public void decrementRetryLeft() {
        if (retryCount == -1)
            return;

        retryCount--;
    }

    public boolean canRetry() {
        if (retryCount == -1)
            return true;
        return (retryCount != 0);
    }

    public void setURL(String url) {
        this.stringURL = url;
    }

    public void addDependant(Request dependentR) {
        dependent.add(dependentR);
    }

    public void addHeader(String key, String val) {
        header.put(key, val);
    }

    public ArrayList<Request> getDependentRequests() {
        return dependent;
    }

    public void setExceptionListener(OnExceptionListener f) {
        this.internalFailedListener = f;
    }

    public OnExceptionListener getInternalFailedListener() {
        return internalFailedListener;
    }

    public void setRequestFailedListener(FailedListener f) {
        this.requestFailedListener = f;
    }

    public FailedListener getRequestFailedListener() {
        return requestFailedListener;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected void validateParameters() {
        if (stringURL == null)
            throw new NullPointerException("URL is null. key:" + getDebugKey());

        if (method == null)
            throw new NullPointerException("Method is null, please add method " +
                    "e.g. POST,PUT etc. key:" + getDebugKey());

        if (contentType == null)
            throw new NullPointerException("Content Type is null, please add Content-Type " +
                    "e.g. application/json. key:" + getDebugKey());
    }

    protected HttpURLConnection getConnection() throws IOException {
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

    public String getErrorMessage() {
        if (errorMessage == null)
            return "";
        return errorMessage;
    }

    public void setDebugKey(String mes) {
        this.debugKey = mes;
    }

    public String getDebugKey() {
        if (debugKey == null)
            return "<no key>";

        return debugKey;
    }

    public abstract int executed();

    /**
     * The builder class for the {@link Request}.
     *
     * @param <T>  The derived {@link Request} class.
     * @param <T1> The derived {@link com.elmargomez.dominohttp.request.Request.Builder} class.
     */
    public static abstract class Builder<T extends Request, T1> {

        private T buildClass;

        public Builder(Class<T> tClass) {
            try {
                buildClass = tClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public T1 setContentType(String string) {
            buildClass.setContentType(string);
            return (T1) this;
        }

        public T1 setMethod(String method) {
            buildClass.setMethod(method);
            return (T1) this;
        }

        public T1 setRetryCount(int c) {
            buildClass.setRetryCount(c);
            return (T1) this;
        }

        public T1 setURL(String url) {
            buildClass.setURL(url);
            return (T1) this;
        }

        public T1 addDependant(Request request) {
            buildClass.addDependant(request);
            return (T1) this;
        }

        public T1 addHeader(String key, String val) {
            buildClass.addHeader(key, val);
            return (T1) this;
        }

        public T1 setExceptionListener(OnExceptionListener f) {
            buildClass.setExceptionListener(f);
            return (T1) this;
        }

        public T1 setRequestFailedListener(FailedListener f) {
            buildClass.setRequestFailedListener(f);
            return (T1) this;
        }

        public T1 setDebugKey(String mes) {
            buildClass.setDebugKey(mes);
            return (T1) this;
        }

        protected T getBuildClass() {
            return buildClass;
        }

        public abstract T build();

    }
}
