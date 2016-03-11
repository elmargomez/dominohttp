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

import java.util.concurrent.BlockingQueue;

public class RequestQueue {
    private static final int MIN_REQUEST_DISPATCHER_COUNT = 3;

    private BlockingQueue<Request> requests = null;
    private BlockingQueue<Request> waitingList = null;

    public void add(Request request) {
        waitingList.add(request);
    }

    public void remove(Request request) {

    }

    public void start() {

    }

    public void stop() {

    }

}
