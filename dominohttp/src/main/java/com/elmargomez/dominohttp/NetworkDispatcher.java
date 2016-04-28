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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class NetworkDispatcher extends Thread {

    private BlockingQueue<Request> networkRequest;
    private Cache cache;
    private ResponseSender responseSender;
    private Network network;

    private boolean isInterrupted;

    public NetworkDispatcher(BlockingQueue<Request> networkRequest, Cache cache, Network network,
                             ResponseSender responseSender) {
        this.networkRequest = networkRequest;
        this.cache = cache;
        this.network = network;
        this.responseSender = responseSender;
    }


    @Override
    public void run() {
        cache.initialize();
        while (true) {
            Request request = null;
            try {
                request = networkRequest.take();
            } catch (InterruptedException e) {
                if (isInterrupted) {
                    return;
                }
            }

            if (request.isCanceled()) {
                // We need to skip this request.
                continue;
            }

            try {
                Network.Response networkResponse = network.getNetworkResponse(request);
                if (networkResponse.responseCode != 200) {
                    // If we have retry, lets just add this back to the Queue
                    int r = request.getRetryCount();
                    if (r > 0) {
                        request.decRetryCount();
                        networkRequest.add(request);
                    } else {
                        responseSender.failure(request, "Unable to get response!");
                    }
                    continue;
                }

                if (request.shouldCached()) {
                    Cache.Data data = new Cache.Data(networkResponse);
                    cache.put(request.getRequestKey(), data);
                }

                responseSender.success(request, networkResponse.serverData);
            } catch (IOException e) {
                int r = request.getRetryCount();
                if (r > 0) {
                    request.decRetryCount();
                    networkRequest.add(request);
                } else {
                    responseSender.failure(request, "Exception: " + e.getMessage());
                }
            }
        }
    }

    public void close() {
        isInterrupted = true;
        interrupt();
    }

}
