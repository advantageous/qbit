package io.advantageous.qbit.annotation.http;

import io.advantageous.qbit.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to map Service method to URIs in an HTTP like protocol.
 *
 * @author Rick Hightower
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface DELETE {


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
    RequestMethod[] method() default {RequestMethod.DELETE};

    /**
     * Used to document endpoint
     *
     * @return description
     */
    String description() default "no description";


    /**
     * Used to document endpoint
     *
     * @return description
     */
    String returnDescription() default "no return description";

    /**
     * Used to document endpoint
     *
     * @return summary
     */
    String summary() default "no summary";


    /**
     * If successful and not set to -1, this will be the HTTP response code returned.
     * If set to -1, then it it will be 200 (OK) if no exception is thrown and a return type or Callback is defined.
     * Otherwise it will be a 202 (ACCEPTED) if there are no callbacks or a return.
     * Note that if you want to get exceptions reported, you have to define a callback or return.
     * This is only used for methods not classes.
     *
     * @return code
     */
    int code() default -1;

    /**
     * ContentType
     * application/javascript
     *
     * @return contentType
     */
    String contentType() default "application/json";
}