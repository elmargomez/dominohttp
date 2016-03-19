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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Request {

    protected ArrayList<Request> dependent;
    protected HashMap<String, String> header;

    // the listener for our request.
    private OnInternalFailedListener internalFailedListener;
    private OnRequestFailedListener requestFailedListener;

    protected int retryCount;
    protected int failureCount;
    protected String contentType = null;
    protected String method = null;
    protected String stringURL = null;

    public Request() {
        this.dependent = new ArrayList<>();
        this.header = new HashMap<>();
        this.contentType = ContentType.APPLICATION_JSON;
        this.retryCount = -1;
    }

    public Request setContentType(String string) {
        this.contentType = string;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public Request setRetryCount(int c) {
        this.retryCount = c;
        return this;
    }

    public Request setURL(String url) {
        this.stringURL = url;
        return this;
    }

    public Request addDependant(Request dependentR) {
        if (dependent == null)
            dependent = new ArrayList<>();

        dependent.add(dependentR);
        return this;
    }

    public Request addHeader(String key, String val) {
        header.put(key, val);
        return this;
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

    public abstract void execute();

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
