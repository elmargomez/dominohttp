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

public class Domino {

    private RequestQueue requestQueue;

    private Domino() {

        requestQueue = new RequestQueue();
    }

    public static Domino getInstance() {
        Domino domino = new Domino();
        domino.start();
        return domino;
    }

    private void start() {
        requestQueue.start();
    }

    public void stop() {
        requestQueue.stop();
    }

    public void add(Request request) {
        requestQueue.add(request);
    }

}
