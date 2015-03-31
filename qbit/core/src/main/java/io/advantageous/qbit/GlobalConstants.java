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

package io.advantageous.qbit;

public class GlobalConstants {

    public final static int BATCH_SIZE = Integer.valueOf(System.getProperty("io.advantageous.qbit.BATCH_SIZE", "1000"));

    public final static int POLL_WAIT = Integer.valueOf(System.getProperty("io.advantageous.qbit.POLL_WAIT", "15"));

    public final static boolean DEBUG = Boolean.valueOf(System.getProperty("io.advantageous.qbit.DEBUG", "false"));

    public final static int NUM_BATCHES = Integer.valueOf(System.getProperty("io.advantageous.qbit.NUM_BATCHES", "100000"));

}
