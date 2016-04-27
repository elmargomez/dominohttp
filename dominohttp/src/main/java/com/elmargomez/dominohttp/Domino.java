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

import android.content.Context;

import java.io.File;

public class Domino {
    private static Domino domino;
    private RequestQueue requestQueue;

    private Domino(Context context) {
        File cFile = new File(context.getCacheDir(), "Fcache");
        FileCache cache = new FileCache(cFile);
        requestQueue = new RequestQueue(cache);
    }

    public static Domino getInstance(Context context) {
        if (domino == null)
            domino = new Domino(context);

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

    public void remove(Request request) {
        requestQueue.remove(request);
    }

}
