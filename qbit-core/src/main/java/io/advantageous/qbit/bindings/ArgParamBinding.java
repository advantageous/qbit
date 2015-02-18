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

package io.advantageous.qbit.bindings;


/**
 * @author Rick Hightower
 *         <p>
 *         Created by Richard on 7/22/14.
 */
public class ArgParamBinding {

    final int methodParamPosition;
    final int uriPosition;
    final String methodParamName;

    public ArgParamBinding(int methodParamPosition, int uriPosition, String methodParamName) {
        this.methodParamPosition = methodParamPosition;
        this.uriPosition = uriPosition;
        this.methodParamName = methodParamName;
    }

    public String getMethodParamName() {
        return methodParamName;
    }

    public int getUriPosition() {
        return uriPosition;
    }

    public int getMethodParamPosition() {
        return methodParamPosition;
    }

    @Override
    public String toString() {
        return "ArgParamBinding{" +
                "methodParamPosition=" + methodParamPosition +
                ", uriPosition=" + uriPosition +
                ", methodParamName='" + methodParamName + '\'' +
                '}';
    }
}
