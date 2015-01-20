package io.advantageous.qbit.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Rick Hightower
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {


    /**
     * Primary mapping expressed by this annotation.
     * For HTTP, this would be the URI. Or part of the URI after the parent URI context
     * be it ServletApp Context or some other parent context.
     *
     * @return a request mapping, URIs really
     */
    String[] value() default {};

    /**
     * HTTP request methods must be:
     * GET, or POST or WebSocket.
     *
     * @return or RequestMethods that are supported by this end point
     */
    RequestMethod[] method() default {RequestMethod.GET};
}
