package io.advantageous.qbit.meta;

public enum CallType {

    /** Passing object name and method name. */
    DIRECT_CALL,
    /** Passing the URI which can be directly looked up. */
    ADDRESS,
    /** Can't just use the direct URI must match start of URI. */
    ADDRESS_WITH_PATH_PARAMS


}
