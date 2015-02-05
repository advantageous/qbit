package io.advantageous.qbit.annotation;

import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.MethodAccess;

/**
 * Created by rhightower on 2/4/15.
 */
public class AnnotationUtils {


    public static AnnotationData getListenAnnotation(MethodAccess methodAccess) {
        AnnotationData listen = methodAccess.annotation("Listen");

        if (listen == null) {
            listen = methodAccess.annotation("OnEvent");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Subscribe");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Consume");
        }

        if (listen == null) {
            listen = methodAccess.annotation("Hear");
        }
        return listen;
    }

}
