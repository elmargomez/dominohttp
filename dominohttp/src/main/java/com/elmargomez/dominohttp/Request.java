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

public class Request {

    private Dependent dependency = null;
    private String contentType = null;
    private String url = null;

    public Request() {
        this.contentType = ContentType.APPLICATION_JSON;
    }

    public Request setContentType(String string) {
        this.contentType = string;
        return this;
    }

    public Request setURL(String url) {
        this.url = url;
        return this;
    }

    public Request dependsOn(Dependent dependent) {
        this.dependency = dependent;
        return this;
    }

    public boolean isDepending() {
        return dependency != null;
    }

    public Dependent getDependency() {
        return dependency;
    }

}
