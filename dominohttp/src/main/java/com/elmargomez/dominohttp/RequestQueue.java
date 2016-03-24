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

import com.elmargomez.dominohttp.request.Request;

import java.util.concurrent.PriorityBlockingQueue;

public class RequestQueue {
    private static final int MIN_REQUEST_DISPATCHER_COUNT = 3;

    private boolean isRunning;
    private RequestDispatcher[] dispatchers;
    private PriorityBlockingQueue<Request> requestsQueue = null;

    public RequestQueue(int dispatcherCount) {
        requestsQueue = new PriorityBlockingQueue();
        this.dispatchers = new RequestDispatcher[dispatcherCount];
    }

    public RequestQueue() {
        this(MIN_REQUEST_DISPATCHER_COUNT);
    }

    public void add(Request request) {
        requestsQueue.add(request);
    }

    public void remove(Request request) {

    }

    public void start() {
        if (isRunning)
            return;

        isRunning = true;
        int c = dispatchers.length;
        for (int i = 0; i < c; i++) {
            dispatchers[i] = new RequestDispatcher(requestsQueue);
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
