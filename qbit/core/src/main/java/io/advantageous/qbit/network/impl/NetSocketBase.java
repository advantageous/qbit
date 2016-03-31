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

package io.advantageous.qbit.network.impl;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.network.NetSocket;
import io.advantageous.qbit.network.NetworkSender;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket like thing that receives messages.
 * Could be mapped to non-websocket implementations.
 */
public class NetSocketBase implements NetSocket {

    private final String remoteAddress;
    private final String uri;
    private final AtomicBoolean open = new AtomicBoolean();
    private boolean binary; //this can't be final because we need the frame handler to know if the message is binary
    private NetworkSender networkSender;
    private Consumer<String> textMessageConsumer = text -> {
    };
    private Consumer<byte[]> binaryMessageConsumer = bytes -> {
    };
    private Consumer<Void> closeConsumer = aVoid -> {
    };
    private Consumer<Void> openConsumer = aVoid -> {
    };
    @SuppressWarnings("CodeBlock2Expr")
    private Consumer<Exception> errorConsumer = error -> {

        LoggerFactory.getLogger(NetSocketBase.class)
                .error(error.getMessage(), error);
    };

    public NetSocketBase(String remoteAddress, String uri, boolean open, boolean binary,
                         NetworkSender networkSender) {
        this.remoteAddress = remoteAddress;
        this.uri = uri;
        this.open.set(open);
        this.binary = binary;
        this.networkSender = networkSender;
    }

    @Override
    public String remoteAddress() {
        return remoteAddress;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public boolean isBinary() {
        return binary;
    }

    @Override
    public void onTextMessage(String message) {
        this.textMessageConsumer.accept(message);
    }

    @Override
    public void onBinaryMessage(byte[] bytes) {
        this.binaryMessageConsumer.accept(bytes);
    }

    @Override
    public void onClose() {
        open.set(false);
        this.closeConsumer.accept(null);
    }

    @Override
    public void onOpen() {
        open.set(true);
        this.openConsumer.accept(null);
    }

    @Override
    public void onError(Exception exception) {
        open.set(false);
        errorConsumer.accept(exception);
    }

    @Override
    public void sendText(String text) {
        try {
            networkSender.sendText(text);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void sendBinary(byte[] bytes) {
        try {
            networkSender.sendBytes(bytes);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public boolean isClosed() {
        return !open.get();
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    public void setBinary() {
        this.binary = true;
    }

    @Override
    public void setTextMessageConsumer(Consumer<String> textMessageConsumer) {
        this.textMessageConsumer = textMessageConsumer;
    }

    @Override
    public void setBinaryMessageConsumer(Consumer<byte[]> binaryMessageConsumer) {
        this.binaryMessageConsumer = binaryMessageConsumer;
    }

    @Override
    public void setCloseConsumer(Consumer<Void> closeConsumer) {
        this.closeConsumer = closeConsumer;
    }

    @Override
    public void setOpenConsumer(Consumer<Void> openConsumer) {
        this.openConsumer = openConsumer;
    }

    @Override
    public void setErrorConsumer(Consumer<Exception> exceptionConsumer) {
        this.errorConsumer = exceptionConsumer;
    }

    @Override
    public void close() {
        try {
            networkSender.close();
        } catch (Exception ex) {
            onError(ex);
        }

    }

    @Override
    public void open() {
        try {
            networkSender.open(this);
        } catch (Exception ex) {
            onError(ex);
        }
    }


    @Override
    public void openAndWait() {

        open();
        /* Try to open for three seconds. */
        int count = 5;
        while (!open.get()) {
            Sys.sleep(50);
            count--;
            if (count <= 0) {
                throw new IllegalStateException("Unable to open WebSocket connection");
            }
        }
    }

    public void setSender(NetworkSender networkSender) {
        this.networkSender = networkSender;
    }
}
