package io.advantageous.qbit.events;

import io.advantageous.qbit.service.Callback;

/**
 * Created by rhightower on 2/3/15.
 */
public class EventUtils {

    public static <T>  EventListener<T> callbackEventListener(final Callback<T> callback) {

        return event -> callback.accept(event.body());
    }
}
