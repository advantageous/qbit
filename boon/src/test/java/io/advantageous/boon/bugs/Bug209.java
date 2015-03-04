package io.advantageous.boon.bugs;

import io.advantageous.boon.core.Dates;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 9/5/14.
 */
public class Bug209 {

    @Test
    public void testIsoJacksonLongDateOffsetVariation() {
        // Colon in the offset: +0200 vs. +02:00
        String test = "2014-05-29T08:54:09.764+02:00";
        Date date = Dates.fromISO8601Jackson(test);

        puts(date);
        Date date2 = Dates.fromISO8601Jackson_(test);
        puts(date2);

        assertEquals( date2.toString(), "" + date );
    }
}
