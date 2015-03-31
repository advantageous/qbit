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

package io.advantageous.qbit.message.impl;

import io.advantageous.qbit.message.Response;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResponseImplTest {


    Response<Object> response;
    Response<Object> response2;

    @Before
    public void setUp() throws Exception {
        response = ResponseImpl.response(10, 11, "/uri", "/returnAddress", "love", null, true);
        response2 = ResponseImpl.response(10, 11, "/uri", "/returnAddress", "love", null, true);

    }


    @Test
    public void test() throws Exception {

        Assert.assertTrue(response.equals(response2));

        Assert.assertTrue(response.hashCode() == response2.hashCode());

        System.out.println(response);
    }

}