/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.util.Timer;
import io.advantageous.boon.core.Sys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Boon.puts;


/**
 * Created by rhightower on 2/2/15.
 */
public class ActualService {

    final Map<Integer, String> map = new HashMap<Integer, String>();

    long lastWrite = Timer.timer().now();

    public static void main(String... args) {

        for (int index = 0; index < 10; index++) {
            long start = System.currentTimeMillis();

            ActualService actualService = new ActualService();
            puts(actualService.addKey(0, "foo"));

            long stop = System.currentTimeMillis();

            long duration = stop - start;

            puts(duration);
        }
        ActualService actualService = new ActualService();

        puts(actualService.addKey(1, "foo"));


        puts(actualService.addKey(3, "foo"));

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
            for (long index = 0; index < 100_000L; index++) {

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
