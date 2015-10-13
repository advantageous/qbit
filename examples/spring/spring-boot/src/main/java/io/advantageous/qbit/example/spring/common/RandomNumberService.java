package io.advantageous.qbit.example.spring.common;

/**
 * Interface for the example service.  This is not totally necessary for qbit, but is nice to have to compare with the
 * async interface for method signatures.  The async interface should be the same as this, but with only void return
 * types, and an additional {@link io.advantageous.qbit.reactive.Callback Callback} with the original return type as the
 * generic type.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
public interface RandomNumberService {
    int getRandom(int min, int max);
}
