/*
 * Copyright 2013-2014 Richard M. Hightower
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
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.bugs;

import io.advantageous.com.examples.model.test.FrequentPrimitives;
import io.advantageous.boon.json.*;
import io.advantageous.boon.bugs.data.media.Image;
import io.advantageous.boon.bugs.data.media.MediaContent;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;


/**
 * Created by Richard on 5/3/14.
 */
public class Bug173_174 {

    @Test
    public void test() {

        final ObjectMapper mapper =  JsonFactory.createUseProperties(true);


        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        mapper.writeValue(stream, FrequentPrimitives.getArray(2));

        puts(new String(stream.toByteArray()));
    }


    @Test
    public void test2() {

        final ObjectMapper mapper =  JsonFactory
                .createUseProperties(true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //String uri, String title, int width, int height, Size size
        mapper.writeValue(stream, new Image("/foo", "Foo", 5, 10, Image.Size.SMALL));


        puts(new String(stream.toByteArray()));
    }



    @Test
    public void test3() {

        final ObjectMapper mapper =  JsonFactory
                .createUseProperties(true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        MediaContent mediaContent = MediaContent.mediaContent();

        //String uri, String title, int width, int height, Size size
        mapper.writeValue(stream, mediaContent);


        MediaContent mediaContent2 = mapper.readValue(stream.toByteArray(), MediaContent.class);

        boolean ok = mediaContent.equals(mediaContent2) || die();
    }



    final JsonSerializer serializer = new JsonSerializerFactory().useFieldsOnly().create();
    final JsonParserAndMapper parser = new JsonParserFactory().create();

    @Test
    public void test4() {



        MediaContent mediaContent = MediaContent.mediaContent();

        String json = serializer.serialize(mediaContent).toString();

        final MediaContent mediaContent1 = parser.parse(MediaContent.class, json);

        boolean ok = mediaContent.equals(mediaContent1) || die();



    }

    @Test
    public void test5() {


        for (int index=0; index < 100; index++) {

            test4();
        }
    }



    @Test
    public void test6() {

        Runnable task =  new Runnable() {
            @Override
            public void run() {

                    for (int index = 0; index < 10_000; index++) {


                        Bug173_174 bugs = new Bug173_174();

                        bugs.test4();
                    }
                }
            };


        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        Thread thread3 = new Thread(task);
        Thread thread4 = new Thread(task);
        Thread thread5 = new Thread(task);

        thread1.start(); thread2.start(); thread3.start();
        thread4.start(); thread5.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static class Data {
        int myint;
        long mylong;

        Data(int myint, long mylong) {
            this.myint = myint;
            this.mylong = mylong;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "myint=" + myint +
                    ", mylong=" + mylong +
                    '}';
        }
    }

        @Test
        public void test198() {


            Data original = new Data(Integer.MAX_VALUE, Long.MAX_VALUE);
            System.out.println("original: \n" + original + "\n");

            ObjectMapper boon = JsonFactory.create();
            String serialized = boon.writeValueAsString(original);
            System.out.println("serialized: \n" + serialized + "\n");

            Data deserialized = boon.readValue(serialized, Data.class);
            System.out.println("deserialized: \n" + deserialized + "\n");

            String reserialized = boon.writeValueAsString(deserialized);
            System.out.println("reserialized: \n" + reserialized + "\n");
        }

}