package io.advantageous.qbit.meta.transformer;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Request;

public interface RequestTransformer {

    MethodCall<Object> transform(Request<Object> message);

}
