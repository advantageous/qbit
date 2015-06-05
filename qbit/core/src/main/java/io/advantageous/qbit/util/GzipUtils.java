package io.advantageous.qbit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {


    public static byte[] encode(String str) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return out.toByteArray();
            //return out.toString(StandardCharsets.ISO_8859_1);
            // Some single byte encoding
        }
    }

    public static String decode(byte[] str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str))) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

}
