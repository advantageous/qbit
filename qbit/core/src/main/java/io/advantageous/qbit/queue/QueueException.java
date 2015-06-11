package io.advantageous.qbit.queue;

public class QueueException extends IllegalStateException {

    public QueueException() {
    }

    public QueueException(String s) {
        super(s);
    }

    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueException(Throwable cause) {
        super(cause);
    }
}
