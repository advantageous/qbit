package io.advantageous.qbit.vertx;

import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;
import static org.junit.Assert.*;

public class BufferUtilsTest {

    boolean ok;

    @Test
    public void testWriteString() throws Exception {

        Buffer buffer = new Buffer();
        BufferUtils.writeString(buffer, "hi mom");

        final short size = buffer.getShort(0);
        ok = size == 6 || die();

        final String utf_8 = buffer.getString(2, size + 2, StandardCharsets.UTF_8.displayName());

        puts(utf_8);

        ok = utf_8.equals("hi mom") || die();

    }

    @Test
    public void testReadString() throws Exception {

        Buffer buffer = new Buffer();
        BufferUtils.writeString(buffer, "hi mom");

        int[] location = new int[]{0};

        String backOut = BufferUtils.readString(buffer, location);


        ok = backOut.equals("hi mom") || die();

    }


    @Test
    public void testWriteMap() throws Exception {

        Buffer buffer = new Buffer();

        MultiMap<String, String> map = new MultiMapImpl<>();

        map.put("key", "value");

        BufferUtils.writeMap(buffer, map);


        int[] location = new int[]{0};


        MultiMap<String, String> map2 = BufferUtils.readMap(buffer, location);

        final String value = map2.get("key");

        ok = value.equals("value") || die();


    }

    @Test
    public void testWriteMapTwoValues() throws Exception {

        Buffer buffer = new Buffer();

        MultiMap<String, String> map = new MultiMapImpl<>();

        map.add("key", "value0");

        map.add("key", "value1");

        BufferUtils.writeMap(buffer, map);


        BufferUtils.writeString(buffer, "body");


        int[] location = new int[]{0};


        MultiMap<String, String> map2 = BufferUtils.readMap(buffer, location);

        final String value = map2.get("key");

        ok = value.equals("value0") || die();

        final Collection<String> values = (Collection<String>) map2.getAll("key");

        ok = values.contains("value1") || die();


        String body = BufferUtils.readString(buffer, location);

        ok = body.equals("body") || die();



    }
}