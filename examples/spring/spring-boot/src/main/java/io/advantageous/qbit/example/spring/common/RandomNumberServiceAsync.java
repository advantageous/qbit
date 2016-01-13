package io.advantageous.qbit.example.spring.common;

import io.advantageous.qbit.reactive.Callback;

/**
 * This is the async interface for the {@link RandomNumberService}.  QBit will create a proxy to the implementation
 * class with this interface because it is specified in the @link io.advantageous.qbit.spring.annotation.QBitService @QBitService} annotation.
 * This interface should contain methods that match the implementation class (or sync interface), but only void return
 * types.  In addition to the matching method arguments, and an additional {@link io.advantageous.qbit.reactive.Callback Callback}
 * argument should be added with the original return type as the generic type.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public interface RandomNumberServiceAsync {
    void getRandom(Callback<Integer> callback, int min, int max);
}
