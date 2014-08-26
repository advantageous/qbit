package org.qbit.bindings;

/**
 * Created by Richard on 7/22/14.
 */
public class ObjectBinding {

    private Class<?> interfaceClass;

    private Object serviceImpl;


    public Class<?> interfaceClass() {
        return interfaceClass;
    }

    public ObjectBinding interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }

    public Object serviceImpl() {
        return serviceImpl;
    }

    public ObjectBinding serviceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
        return this;
    }
}
