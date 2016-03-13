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

public class PriorityDispatcher extends Thread {

    private boolean shouldStop;
    private RequestOrder requestsQueue = null;
    private RequestOrder waitingList = null;

    public PriorityDispatcher(RequestOrder queue, RequestOrder waitingList) {
        this.requestsQueue = queue;
        this.waitingList = waitingList;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request r = waitingList.take();
                if (!r.isDepending() || (!requestsQueue.hasDependent(r)
                        && !waitingList.hasDependent(r))) {

                    requestsQueue.add(r);
                    waitingList.remove(r);
                }
            } catch (InterruptedException e) {
                if (shouldStop)
                    return;
                continue;
            }
        }
    }

    public void close() {
        shouldStop = true;
        interrupt();
    }
}
