/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.annotation;


import java.lang.annotation.*;

/**
 * Used to map Service method to URIs in an HTTP like protocol.
 *
 * @author Rick Hightower
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {


    /**
     * Primary mapping expressed by this annotation.
     * For HTTP, this would be the URI. Or part of the URI after the parent URI context
     * be it ServletApp Context or some other parent context.
     *
     * @return a request mapping, URIs really
     */
    String[] value() default {};

    /**
     * HTTP request methods must be:
     * GET, or POST or WebSocket.
     *
     * @return or RequestMethods that are supported by this end point
     */
    RequestMethod[] method() default {RequestMethod.GET};

    /**
     * Used to document endpoint
     *
     * @return description
     */
    String description() default "no description";


    /**
     * Used to document endpoint
     *
     * @return description
     */
    String returnDescription() default "no return description";

    /**
     * Used to document endpoint
     *
     * @return summary
     */
    String summary() default "no summary";


    /**
     * If successful and not set to -1, this will be the HTTP response code returned.
     * If set to -1, then it it will be 200 (OK) if no exception is thrown and a return type or Callback is defined.
     * Otherwise it will be a 202 (ACCEPTED) if there are no callbacks or a return.
     * Note that if you want to get exceptions reported, you have to define a callback or return.
     * This is only used for methods not classes.
     *
     * @return code
     */
    int code() default -1;

    /**
     * ContentType
     * application/javascript
     *
     * @return contentType
     */
    String contentType() default "application/json";
}
