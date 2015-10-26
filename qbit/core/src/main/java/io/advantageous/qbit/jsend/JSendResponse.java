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

    public JSendResponse(final Class<T> type, final T value) {
        this.type = type;
        this.data = value;
    }


    public JSendResponse(final T value) {
        this.type =  (Class<T>) (Object) Object.class;
        this.data = value;
    }


}
