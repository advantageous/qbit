package io.advantageous.qbit.guice;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by marat on 10/3/15
 */
@Target({TYPE, METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Inherited
@Retention(RUNTIME)
public @interface Auth {
    boolean pass() default true;

    /**
     * Defines several {@code @NotBlank} annotations on the same element.
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        Auth[] value();
    }

}
