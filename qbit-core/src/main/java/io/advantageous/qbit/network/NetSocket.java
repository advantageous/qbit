package io.advantageous.qbit.network;

import java.util.function.Consumer;

/**
 * Created by rhightower on 2/14/15.
 */
public interface NetSocket {

    String remoteAddress();
    String uri();
    void onTextMessage(String message);
    void onBinaryMessage(byte[] bytes);
    void onClose();
    void onOpen();
    void onError(Exception exception);
    void sendText(String string);
    void sendBinary(byte[] bytes);
    boolean isClosed();
    boolean isOpen();
    boolean isBinary();
    void setTextMessageConsumer(Consumer<String> textMessageConsumer);
    void setBinaryMessageConsumer(Consumer<byte[]> binaryMessageConsumer);
    void setCloseConsumer(Consumer<Void> closeConsumer);
    void setOpenConsumer(Consumer<Void> openConsumer);
    void setErrorConsumer(Consumer<Exception> exceptionConsumer);
    void close();
    void open();
    void openAndWait();


}
