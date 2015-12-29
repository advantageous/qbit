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

package io.advantageous.qbit.sender;

import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.BeforeMethodCall;
import io.advantageous.qbit.service.EndPoint;
import io.advantageous.qbit.service.impl.NoOpBeforeMethodCall;
import io.advantageous.qbit.spi.ProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Combines a sender with a protocol encoder so we can forward messages to another remote end point.
 * created by Richard on 10/1/14.
 *
 * @author Rick Hightower
 */
public class SenderEndPoint implements EndPoint {


    private final ProtocolEncoder encoder;
    private final String address;
    private final Sender<String> sender;
    private final BeforeMethodCall beforeMethodCall;
    private final BlockingQueue<MethodCall<Object>> methodCalls;
    private final Logger logger = LoggerFactory.getLogger(SenderEndPoint.class);

    public SenderEndPoint(ProtocolEncoder encoder, String address, Sender<String> sender, BeforeMethodCall beforeMethodCall,
                          int requestBatchSize) {
        this.encoder = encoder;
        this.address = address;
        this.beforeMethodCall = beforeMethodCall == null ? new NoOpBeforeMethodCall() : beforeMethodCall;
        this.methodCalls = new ArrayBlockingQueue<>(requestBatchSize);
        this.sender = sender;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public void call(MethodCall<Object> methodCall) {

        beforeMethodCall.before(methodCall);

        if (!methodCalls.offer(methodCall)) {
            flush();
        } else {
            return;
        }


        if (!methodCalls.offer(methodCall)) {
            throw new IllegalStateException("Queue is full and can't be emptied");
        }



    }

    @Override
    public void call(List<MethodCall<Object>> methodCalls) {

        if (methodCalls.size() > 0) {
            String returnAddress = methodCalls.get(0).returnAddress();
            @SuppressWarnings("unchecked") List<Message<Object>> methods = (List<Message<Object>>) (Object) methodCalls;
            sender.send(returnAddress, encoder.encodeAsString(methods));
        }
    }



    @Override
    public void flush() {

        if (methodCalls.size() > 0) {


            Message<Object> method;

            List<Message<Object>> methods;


            methods = new ArrayList<>(methodCalls.size());


            do {
                method = methodCalls.poll();
                methods.add(method);
            }while (method!=null);


            if (methods.size() > 0) {
                sender.send(((MethodCall<Object>) methods.get(0)).returnAddress(), encoder.encodeAsString(methods));
            }

        }

    }

    public void stop() {
        try {
            flush();
        } catch (Exception ex) {
            logger.warn("Unable to flush before stop", ex);
        }
        sender.stop();
    }
}
