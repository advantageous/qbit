package io.advantageous.qbit.jsend;

import io.advantageous.boon.json.annotations.JsonInclude;
import io.advantageous.qbit.annotation.JsonIgnore;

/**
 * https://labs.omniti.com/labs/jsend
 */
public class JSendResponse<T> {

    @JsonIgnore
    private final Class<T> type;


    @JsonInclude
    private final T data;

    private final String status;

    public JSendResponse(final Class<T> type, final T value, final JSendStatus status) {
        this.type = type;
        this.data = value;
        this.status = status.toString();
    }


    @Override
    public String toString() {
        return "JSendResponse{" +
                "type=" + type +
                ", data=" + data +
                ", status=" + status +
                '}';
    }
}
