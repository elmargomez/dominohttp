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

package com.elmargomez.dominohttp.networking;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.elmargomez.dominohttp.data.CustomNetwork;
import com.elmargomez.dominohttp.data.FileCache;
import com.elmargomez.dominohttp.data.ResponseSender;
import com.elmargomez.dominohttp.networking.util.DominoLog;
import com.elmargomez.dominohttp.networking.worker.CacheDispatcher;
import com.elmargomez.dominohttp.networking.worker.NetworkDispatcher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Create the abstract request
 */
public class Request implements Comparable {

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";

    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE_JPEG = "image/jpeg";

    public static final int IDLE = 0;
    public static final int QUEUED = 1;
    public static final int CANCELED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GET, PUT, POST})
    public @interface Method {

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({APPLICATION_JSON, TEXT_PLAIN, IMAGE_JPEG})
    public @interface ContentType {

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IDLE, QUEUED, CANCELED})
    public @interface State {

    }

    private int mRetrievalID;
    private Success mSuccess;
    private Error mError;

    public Map<String, String> header;
    private String mRequestID;
    public String url;
    public String method;
    public String contentType;
    private byte[] mRequestBody;

    @State
    public int state = IDLE;
    public boolean shouldCached = true;
    public int retryLeft = 3;

    public Request(String requestID, String URL, @Method String met,
                   @ContentType String ct, byte[] body, int retrievalID, Success success,
                   Error error) {
        header = new HashMap<>();
        mRequestID = requestID;
        url = URL;
        method = met;
        contentType = ct;
        mRequestBody = body;
        mRetrievalID = retrievalID;
        mSuccess = success;
        mError = error;
    }

    public String getRequestKey() {
        return mRequestID;
    }

    public void release() {
        mSuccess = null;
        mError = null;
    }

    public byte[] getRequestByte() {
        return mRequestBody;
    }

    public Success getSuccessListener() {
        return mSuccess;
    }

    public Error getErrorListener() {
        return mError;
    }

    @Override
    public int compareTo(Object another) {
        // TODO Implement later
        return 0;
    }

    public interface Success {
        void response(int retrievalID, byte[] response);
    }

    public interface Error {
        void response(int retrievalID, String s);
    }

    /**
     * The cache Queue.
     */
    public static class Queue {
        private static final int MIN_REQUEST_DISPATCHER_COUNT = 4;

        private final BlockingQueue<Request> networkRequest = new PriorityBlockingQueue<>();
        private final BlockingQueue<Request> cachedRequest = new PriorityBlockingQueue<>();
        private boolean isRunning;

        private Network mNetwork;
        private Cache mCache;
        private Callback mCallback;

        private NetworkDispatcher[] dispatchers;
        private CacheDispatcher cacheDispatcher;

        /**
         * Creates the Queue and all worker Thread behind the scene.
         *
         * @param network
         * @param cache
         * @param dispatcherCount
         */
        public Queue(Network network, Cache cache, Callback callback, int dispatcherCount) {
            if (network == null) {
                network = new CustomNetwork();
            }
            this.mNetwork = network;
            this.mCache = cache;
            this.mCallback = callback;
            this.dispatchers = new NetworkDispatcher[dispatcherCount];
        }

        public Queue(Cache mCache) {
            this(null, mCache, new ResponseSender(), MIN_REQUEST_DISPATCHER_COUNT);
        }

        /**
         * Remove request from the Queue.
         *
         * @param request
         */
        public synchronized void remove(Request request) {
            request.state = CANCELED;
            synchronized (cachedRequest) {
                cachedRequest.remove(request);
            }
            synchronized (networkRequest) {
                networkRequest.remove(request);
            }
        }

        /**
         * Create and start the request.
         *
         * @param url         the API URL.
         * @param method      the API method.
         * @param contentType the API content-type
         * @param body        the request body.
         * @param retrievalID the listener identifier.
         * @param success     the success listener.
         * @param error       the error listener.
         * @return the remote request.
         */
        public Request create(String url, String method, String contentType,
                              byte[] body, int retrievalID, Success success,
                              Error error) {
            return create(UUID.randomUUID().toString(), url, method, contentType, body,
                    retrievalID, success, error);

        }

        /**
         * Create and start the request.
         *
         * @param requestID   the request ID.
         * @param url         the API URL.
         * @param method      the API method.
         * @param contentType the API content-type
         * @param body        the request body.
         * @param retrievalID the listener identifier.
         * @param success     the success listener.
         * @param error       the error listener.
         * @return the remote request.
         */
        private synchronized Request create(String requestID, String url, String method,
                                            String contentType, byte[] body, int retrievalID,
                                            Success success, Error error) {
            synchronized (cachedRequest) {
                Request r = new Request(requestID, url, method, contentType, body, retrievalID,
                        success, error);
                cachedRequest.add(r);
                return r;
            }
        }

        /**
         * Recover the request remote from the Cache.
         *
         * @param requestID the request ID of the request to recover.
         * @return null if request is no longer in the queue, otherwise get the
         * the instance back.
         */
        public synchronized Request recover(String requestID, Success success, Error error) {
            synchronized (cachedRequest) {
                Iterator<Request> iterator = cachedRequest.iterator();
                while (iterator.hasNext()) {
                    Request request = iterator.next();
                    if (request.getRequestKey().equals(requestID)) {
                        return request;
                    }
                }
            }
            synchronized (networkRequest) {
                Iterator<Request> iterator = networkRequest.iterator();
                while (iterator.hasNext()) {
                    Request request = iterator.next();
                    if (request.getRequestKey().equals(requestID)) {
                        return request;
                    }
                }
            }
            synchronized (mCache) {
                FileCache.Data data = mCache.get(requestID);
                if (data != null) {
                    // TODO fixme
//                    return create(data.header.)
                }
                return null;
            }
        }

        /**
         * Start the worker threads.
         */
        public void start() {
            if (isRunning)
                return;

            isRunning = true;
            int c = dispatchers.length;
            for (int i = 0; i < c; i++) {
                dispatchers[i] = new NetworkDispatcher(networkRequest, mCache, mNetwork, mCallback);
                dispatchers[i].start();
            }
            cacheDispatcher = new CacheDispatcher(networkRequest, cachedRequest, mCache, mCallback);
            cacheDispatcher.start();
            DominoLog.debug("Request Queue Started!");
        }

        /**
         * Stop the worker threads.
         */
        public void stop() {
            if (!isRunning)
                return;

            isRunning = false;
            int c = dispatchers.length;
            for (int i = 0; i < c; i++) {
                dispatchers[i].close();
            }
            cacheDispatcher.cancel();
            DominoLog.debug("Request Queue Stopped!");
        }

    }
}
