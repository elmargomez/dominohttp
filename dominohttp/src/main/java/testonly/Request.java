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

package testonly;


import java.util.Comparator;

public class Request<R> implements Comparator<Request> {

    String id = null;

    public Request(SuccessListener<R> listener, ErrorListener errorListener) {

    }

    public Request(SuccessListener<R> listener) {
        this(listener, null);
    }

    @Override
    public int compare(Request lhs, Request rhs) {
        return 0;
    }


    public interface SuccessListener<R> {
        void response(R r);
    }

    public interface ErrorListener {
        void response(String r);
    }

}
