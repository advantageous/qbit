package io.advantageous.qbit.bugs;

import io.advantageous.boon.core.Conversions;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rick on 6/21/16.
 */
public class Bug742 {

    @Test
    public void test() {
        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();

        Map<String, String> data = new HashMap<>();
        data.put("startTime", "2016-06-21T21:10:45.3271564Z");
        data.put("endTime", "2016-06-21T21:11:42.5356272Z");
        String dataString = jsonMapper.toJson(data);

        final DateFecker query = jsonMapper.fromJson(dataString, DateFecker.class);


        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:10:45.3271564Z"));
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:11:45.5356272Z"));
        System.out.println();
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:10:45Z"));
        System.out.println(Conversions.coerce(LocalDateTime.class, "2016-06-21T21:11:45Z"));

    }

    class DateFecker {
        public LocalDateTime startTime;
        public LocalDateTime endTime;
    }
}
