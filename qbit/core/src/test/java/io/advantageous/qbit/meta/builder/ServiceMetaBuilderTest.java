package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rick on 10/26/15.
 */
public class ServiceMetaBuilderTest {


    public static class Foo {


        @RequestMapping("/foo")
        public int foo() {
            return 0;
        }
    }

    @Test
    public void test() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("foo", Lists.list(classMeta.methods()));


        final ServiceMethodMeta serviceMethodMeta =
                serviceMetaBuilder.getMethods().get(0);


        assertEquals(int.class, serviceMethodMeta.getReturnType());
    }

}