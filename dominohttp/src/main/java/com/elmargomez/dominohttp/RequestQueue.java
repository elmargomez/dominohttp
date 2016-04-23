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

import com.elmargomez.dominohttp.request.Cache;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class RequestQueue {
    private static final int MIN_REQUEST_DISPATCHER_COUNT = 4;

    private final ArrayList<String> requestLog = new ArrayList<>();
    private final PriorityBlockingQueue<Request> networkRequest = new PriorityBlockingQueue<>();
    private final PriorityBlockingQueue<Request> cachedRequest = new PriorityBlockingQueue<>();
    private boolean isRunning;

    private Cache cache = null;
    private Network network = null;
    private NetworkDispatcher[] dispatchers;
    private ResponseSender sender;

    public RequestQueue(Network network, Cache cache, int dispatcherCount) {

        if (network == null) {
            network = new Network();
        }



        this.network = network;
        this.cache = cache;
        this.dispatchers = new NetworkDispatcher[dispatcherCount];
        this.sender = new ResponseSender();
    }

    public RequestQueue() {

        this(null, null, MIN_REQUEST_DISPATCHER_COUNT);
    }

    public void add(Request request) {
        synchronized (requestLog) {
            String key = request.getRequestKey();

            // Check if this request was already here.
            if (requestLog.contains(request.getRequestKey())) {
                // This request was processed before, but we need to decided
                // on what to do with it.
                if (request.shouldCached()) {

                } else {

                }
            } else {
                // Since this request is not yet requested before,
                // we are going to put directly to the request Queue.
                requestLog.add(key);
                cachedRequest.add(request);
            }
        }
    }

    public void remove(Request request) {

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
    }

    public void stop() {
        if (!isRunning)
            return;

        isRunning = false;
        int c = dispatchers.length;
        for (int i = 0; i < c; i++) {
            dispatchers[i].close();
        }
    }

}
