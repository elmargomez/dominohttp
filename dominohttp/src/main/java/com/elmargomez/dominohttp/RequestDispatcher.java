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

public class RequestDispatcher extends Thread {

    private boolean shouldStop;
    private PriorityBlockingQueue<Request> requestOrder;

    public RequestDispatcher(PriorityBlockingQueue order) {
        this.requestOrder = order;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request request = requestOrder.take();
                int req = request.executed();
                switch (req) {
                    case Request.EXECUTION_REQUEST_SUCCESS:
                        // Since the request is successful, It is necessary to execute next
                        // the descending Request.
                        requestOrder.addAll(request.getDependentRequests());
                        break;
                    case Request.EXECUTION_REQUEST_ERROR:
                        // TODO add something later
                        break;
                    case Request.EXECUTION_FAILURE_ON_DEPLOY:
                        if (request.canRetry()) {
                            // While the request fails, let us add it again to the queue
                            // until the retry count reached to 0.
                            request.decrimentRetryLeft();
                            requestOrder.add(request);
                        } else {
                            // Since the request error reached zero lets now fire the callback,
                            // all the other descending request will be dropped.
                            Request.OnExceptionListener listener =
                                    request.getInternalFailedListener();
                            if (listener != null) {
                                listener.response(request);
                            }
                        }
                        break;
                }
            } catch (InterruptedException e) {
                if (shouldStop) {
                    return;
                }
            }
        }
    }

    public void close() {
        shouldStop = true;
        interrupt();
    }
}
