package io.advantageous.qbit.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created by marat on 12/15/15
 */
public class AuthorizedGuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), new AbstractMatcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return getAuthAnnotations(method.getAnnotations()).isPresent();
            }


        }, new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation ctx) throws Throwable {
                Optional<Auth> methodAnnotation = getAuthAnnotations(ctx.getMethod().getAnnotations());

                boolean allowAnon = methodAnnotation.map(Auth::pass).orElse(true);

                if (!allowAnon) {
                    throw new Exception("Access denied");
                }

                return ctx.proceed();
            }
        });
    }

    private Optional<Auth> getAuthAnnotations(Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (Auth.class.equals(ann.annotationType())) {
                return Optional.of((Auth) ann);
            }
        }
        return Optional.empty();
    }
}
