package io.advantageous.qbit.events;

/**
 * Created by rhightower on 2/11/15.
 */
public interface EventBusProxyCreator {


    <T> T createProxy(final EventManager eventManager, final Class<T> eventBusProxyInterface);


    <T> T createProxyWithChannelPrefix(final EventManager eventManager,
                                       final Class<T> eventBusProxyInterface,
                                       final String prefix);




}
