package io.advantageous.qbit.perf;

import io.advantageous.qbit.service.Callback;

/**
 * Created by Richard on 12/7/14.
 */
public interface AdderClientInterface {

    void add(String name, int value);

    void sum(Callback<Integer> callback);
}
