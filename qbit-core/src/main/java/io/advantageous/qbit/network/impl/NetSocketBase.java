package io.advantageous.qbit.network.impl;

import io.advantageous.qbit.network.NetSocket;
import io.advantageous.qbit.network.NetworkSender;

import java.util.function.Consumer;

/**
 * Created by rhightower on 2/14/15.
 */
public class NetSocketBase implements NetSocket {

    private final String remoteAddress;
    private final String uri;
    private  NetworkSender networkSender;
    private boolean open;
    private final boolean binary;
    private Consumer<String> textMessageConsumer = text -> {};
    private Consumer<byte[]> binaryMessageConsumer = bytes -> {};
    private Consumer<Void> closeConsumer = aVoid -> {};
    private Consumer<Void> openConsumer = aVoid -> {};
    private Consumer<Exception> errorConsumer = e -> {};

    public NetSocketBase(String remoteAddress, String uri, boolean open, boolean binary,
                         NetworkSender networkSender) {
        this.remoteAddress = remoteAddress;
        this.uri = uri;
        this.open = open;
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
        open = false;
        this.closeConsumer.accept(null);
    }

    @Override
    public void onOpen() {
        open = true;
        this.openConsumer.accept(null);
    }

    @Override
    public void onError(Exception exception) {
        open = false;
        errorConsumer.accept(exception);
    }

    @Override
    public void sendText(String text) {
        try {
            networkSender.sendText(text);
        }catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void sendBinary(byte[] bytes) {
        try {
            networkSender.sendBytes(bytes);
        }catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public boolean isClosed() {
        return !open;
    }

    @Override
    public boolean isOpen() {
        return open;
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
        }catch (Exception ex) {
            onError(ex);
        }

    }

    @Override
    public void open() {
        try {
            networkSender.open(this);
        }catch (Exception ex) {
            onError(ex);
        }
    }

    public void setSender(NetworkSender networkSender) {
        this.networkSender = networkSender;
    }
}
