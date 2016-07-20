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

package com.elmargomez.dominohttp.inter;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuccessResponse {

    int STRING_RESPONSE = 1;
    int BITMAP_RESPONSE = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STRING_RESPONSE, BITMAP_RESPONSE})
    @interface Response {

    }

    /**
     * This method is called by the API when the ID matches.
     *
     * @return the ID of the success callback.
     */
    int id();

    /**
     * The responseType of the web server.
     *
     * @return
     */
    @Response int responseType();

}
