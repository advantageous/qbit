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

package io.advantageous.qbit.spi;

import io.advantageous.qbit.boon.client.BoonClient;
import io.advantageous.qbit.client.BeforeMethodSent;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.http.client.HttpClient;

/**
 * Creates a client.
 * This gets used by QBit factory to createWithWorkers a client.
 * created by rhightower on 12/3/14.
 *
 * @author rhightower
 */
public interface ClientFactory {

    default Client create(String uri,
                  HttpClient httpClient,
                  int requestBatchSize,
                  final BeforeMethodSent beforeMethodSent) {
        return new BoonClient(uri, httpClient, requestBatchSize, beforeMethodSent);
    }
}
