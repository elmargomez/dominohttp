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

package com.elmargomez.dominohttp.data;

import com.elmargomez.dominohttp.inter.ErrorResponse;
import com.elmargomez.dominohttp.inter.SuccessResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class WebRequest {

    protected WebRequest(Object object, int successID, int errorID) {
        Class<?> classInstance = object.getClass();
        for (Method method : classInstance.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType() == SuccessResponse.class) {

                } else if (annotation.annotationType() == ErrorResponse.class) {

                }
            }
        }
    }

}
