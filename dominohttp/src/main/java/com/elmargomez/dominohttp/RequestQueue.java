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

package com.elmargomez.dominohttp;

import com.elmargomez.dominohttp.data.WebRequest;
import com.elmargomez.dominohttp.request.Request;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RequestQueue {
    private static final int MIN_REQUEST_DISPATCHER_COUNT = 4;

    private final BlockingQueue<WebRequest> networkRequest = new ArrayBlockingQueue<>();
    private final BlockingQueue<WebRequest> cachedRequest = new ArrayBlockingQueue<>();
    private boolean isRunning;

    private Cache cache = null;
    private Network network = null;
    private NetworkDispatcher[] dispatchers;
    private CacheDispatcher cacheDispatcher;
    private WebRequest.ResponseSender sender;

    public RequestQueue(Network network, Cache cache, int dispatcherCount) {

        if (network == null) {
            network = new Network();
        }

        this.network = network;
        this.cache = cache;
        this.dispatchers = new NetworkDispatcher[dispatcherCount];
        this.sender = new WebRequest.ResponseSender();
    }

    public RequestQueue(Cache cache) {
        this(null, cache, MIN_REQUEST_DISPATCHER_COUNT);
    }

    public void add(WebRequest request) {
        synchronized (cachedRequest) {
            cachedRequest.add(request);
        }
    }

    public synchronized void remove(Request request) {
        request.setCanceled(true);
        synchronized (cachedRequest) {
            cachedRequest.remove(request);
        }
        synchronized (networkRequest) {
            networkRequest.remove(request);
        }
    }

    public void start() {
        if (isRunning)
            return;

        isRunning = true;
        int c = dispatchers.length;
        for (int i = 0; i < c; i++) {
            dispatchers[i] = new NetworkDispatcher(networkRequest, cache, network, sender);
            dispatchers[i].start();
        }
        cacheDispatcher = new CacheDispatcher(networkRequest, cachedRequest, cache, sender);
        cacheDispatcher.start();
        DominoLog.debug("Request Queue Started!");
    }

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
