package io.advantageous.qbit.vertx;

import io.advantageous.qbit.util.MultiMap;
import org.vertx.java.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/26/15.
 */
public class BufferUtils {


    public static void writeString(Buffer buffer, String value) {

        byte [] string = value.getBytes(StandardCharsets.UTF_8);
        buffer.appendShort((short) string.length);
        buffer.appendBytes(string);

    }

    public static void writeMap(Buffer buffer, MultiMap<String, String> params) {
        buffer.appendShort((short) params.size());

        final Set<String> keys = params.keySet();

        for (String key : keys) {
            final Collection<String> values = (Collection<String>) params.getAll(key);
            buffer.appendShort((short) values.size());

            for (String value : values) {
                writeString(buffer, value);
            }
        }
    }

    public static String readString(Buffer buffer, int[] location) {

        final short size = buffer.getShort(location[0]);

        final String utf_8 = buffer.getString(location[0] + 2, size + 2, StandardCharsets.UTF_8.displayName());

        location[0] = 2 + size;

        return utf_8;
    }
}
