package io.advantageous.qbit.bindings;

import org.boon.Str;
import org.junit.Test;

import static org.boon.Boon.puts;

public class MethodBindingTest {

    @Test
    public void test() {
        MethodBinding binding = new MethodBinding("GET", "bob", "/hi/how/{are}/{1}");
        puts(binding);

        Str.equalsOrDie("bob", binding.methodName());

        Str.equalsOrDie("/hi/how/", binding.address());

    }
}
