package io.advantageous.qbit.jsend;

public class JSendResponseBuilder<T> {


    private Class<T> type;
    private T data;
    private JSendStatus status = JSendStatus.SUCCESS;

    public static <T> JSendResponseBuilder<T> jSendResponseBuilder() {
        return new JSendResponseBuilder<>();
    }

    public static <T> JSendResponseBuilder<T> jSendResponseBuilder(T value) {

        JSendResponseBuilder<T> resposne = new JSendResponseBuilder<>();
        resposne.setData(value);
        resposne.setType((Class<T>) value.getClass());
        return resposne;
    }

    public Class<T> getType() {
        return type;
    }

    public JSendResponseBuilder setType(Class<T> type) {
        this.type = type;
        return this;
    }

    public T getData() {
        return data;
    }

    public JSendResponseBuilder setData(T data) {
        this.data = data;
        return this;
    }

    public JSendStatus getStatus() {
        return status;
    }

    public JSendResponseBuilder setStatus(JSendStatus status) {
        this.status = status;
        return this;
    }

    public JSendResponse<T> build() {
        return new JSendResponse<>(this.getType(), this.getData(), this.getStatus());
    }
}
