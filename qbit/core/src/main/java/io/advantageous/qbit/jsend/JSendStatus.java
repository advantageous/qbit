package io.advantageous.qbit.jsend;

/**
 * https://labs.omniti.com/labs/jsend
 */
public enum JSendStatus {


    SUCCESS("success"), FAIL("fail"), ERROR("error");

    private final String status;

    JSendStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
