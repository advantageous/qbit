package io.advantageous.qbit.annotation.http;

import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to map Service method to URIs in an HTTP like protocol.
 * NOTE: PUT is for edit or update and a POST is for create; therefore,
 * we change the HTTP status to CREATED.
 *
 * @author Rick Hightower
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface POST {


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
    RequestMethod[] method() default {RequestMethod.POST};

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
     * Since POST is for CREATE or ADD, we change the default HTTP status code to CREATED.
     *
     * @return code
     */
    int code() default HttpStatus.CREATED;

    /**
     * ContentType
     * application/javascript
     *
     * @return contentType
     */
    String contentType() default "application/json";
}