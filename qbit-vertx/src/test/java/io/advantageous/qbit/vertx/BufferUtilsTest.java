package io.advantageous.qbit.vertx;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;

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

    }
}