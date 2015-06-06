package hello;


import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.util.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * QBit Service Example
 * <p>
 * created by rhightower on 2/2/15.
 */

public class ActualService {

    final Map<Integer, String> map = new HashMap<>();
    long lastWrite = Timer.timer().now();

    ActualService() {
        System.out.println("created ActualService");
    }

    public static void main(String... args) {

        for (int index = 0; index < 10; index++) {
            long start = System.currentTimeMillis();

            ActualService actualService = new ActualService();
            System.out.println(actualService.addKey(0, "foo"));

            long stop = System.currentTimeMillis();

            long duration = stop - start;

            System.out.println(duration);
        }
        ActualService actualService = new ActualService();

        System.out.println(actualService.addKey(1, "foo"));
        System.out.println(actualService.addKey(3, "foo"));

    }

    public void write() {

        long now = Timer.timer().now();
        long duration = now - lastWrite;

        if (duration > 5000) {
            lastWrite = now;
            Sys.sleep(200);
        }
    }

    public double addKey(int key, String value) {

        double dvalue = 0.0;
        int ivalue = 0;

        if (key == 0) {
            for (long index = 0; index < 35_000L; index++) {

                dvalue = dvalue + index * 1000;
                ivalue = (int) dvalue;
                ivalue = ivalue % 13;
            }
        } else {


            final Set<Integer> integers = map.keySet();

            for (Integer k : integers) {
                dvalue += k + map.get(k).hashCode();
            }
            map.put(key, value);
            dvalue += map.get(key).hashCode();
        }

        return (dvalue += ((double) ivalue));
    }
}
