package io.advantageous.qbit.jms;

public class JmsNotConnectedException extends JmsException {

    public JmsNotConnectedException(String message) {
        super(message);
    }

    public JmsNotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JmsNotConnectedException(Throwable cause) {
        super(cause);
    }

    public JmsNotConnectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
