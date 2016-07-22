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

package com.elmargomez.dominohttp.networking.worker;

import com.elmargomez.dominohttp.data.CustomNetwork;
import com.elmargomez.dominohttp.data.FileCache;
import com.elmargomez.dominohttp.networking.Cache;
import com.elmargomez.dominohttp.networking.Callback;
import com.elmargomez.dominohttp.networking.Network;
import com.elmargomez.dominohttp.networking.Request;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class NetworkDispatcher extends Thread {

    private BlockingQueue<Request> networkRequest;
    private Cache cache;
    private Callback responseSender;
    private Network customNetwork;

    /**
     * Flag the long running Thread to force stop.
     */
    private boolean isInterrupted;

    public NetworkDispatcher(BlockingQueue<Request> networkRequest, Cache cache,
                             Network network, Callback sender) {
        this.networkRequest = networkRequest;
        this.cache = cache;
        this.customNetwork = network;
        this.responseSender = sender;
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

            if (request.state == Request.CANCELED) {
                // We need to skip this request.
                continue;
            }

            try {
                CustomNetwork.Response networkResponse = customNetwork.getNetworkResponse(request);
                if (request.shouldCached) {
                    FileCache.Data data = new FileCache.Data(networkResponse);
                    cache.put(request.getRequestKey(), data);
                }
                responseSender.success(request, networkResponse.serverData);
            } catch (IOException e) {
                int retryCount = request.retryLeft;
                if (retryCount > 0) {
                    request.retryLeft--;
                    networkRequest.add(request);
                } else {
                    responseSender.failure(request, e.getMessage());
                }
            }
        }
    }

    public void close() {
        isInterrupted = true;
        interrupt();
    }

}
