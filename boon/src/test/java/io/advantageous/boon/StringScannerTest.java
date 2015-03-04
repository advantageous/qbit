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

package io.advantageous.boon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.primitive.Arry.idx;
import static io.advantageous.boon.primitive.Arry.len;
import static io.advantageous.boon.Exceptions.die;
import static org.junit.Assert.assertEquals;

/**
 * Created by rick on 12/8/13.
 */
public class StringScannerTest {


    private static final String TEST_STRING = "[199984,1384795052823,\"/127.0.0.1:51706\",[\"abc123\",\"rickHigh\"," +
            "\"217.0.0.1\",\"start\",1234567,12345678,\"abcsesson123\",\"asdfasdf\"]]";


    boolean ok = true;


    //@Test
    public void speedTestParseInt() {

        int numIter = 50_000_000;

        List<String> numbers =  new ArrayList<>(numIter);

        for (int index =0; index < numIter; index++) {
            numbers.add("" + index);
        }
        long total = 0;

        puts("Number generated", numbers.size());

        for (String num : numbers) {
            final int i = StringScanner.parseInt(num);
            total += i;
        }

        puts (total);
        total = 0;
        long start = System.currentTimeMillis();
        for (String num : numbers) {
            final int i = StringScanner.parseInt(num);
            total += i;
        }
        long stop = System.currentTimeMillis();

        long duration = stop - start;
        puts("new parse", duration, total);



        start =System.currentTimeMillis();
        total = 0;

        for (String num : numbers) {
            final int i = Integer.parseInt(num);
            total += i;
        }
        stop = System.currentTimeMillis();

        duration = stop - start;


        puts("old parse", duration, total);

    }


    //@Test
    public void speedTestParseLong() {

        int numIter = 10_000_000;

        long BIG_NUM = Integer.MAX_VALUE;

        List<String> numbers =  new ArrayList<>(numIter);

        for (int index =0; index < numIter; index++) {
            numbers.add("" + (BIG_NUM+ index));
        }
        long total = 0;

        puts("Number generated", numbers.size(), BIG_NUM);

        for (String num : numbers) {
            final long i = StringScanner.parseLong(num);
            total += i;
        }

        puts (total);
        total = 0;
        long start = System.currentTimeMillis();

        for (String num : numbers) {
            final long i = StringScanner.parseLong(num);
            total += i;
        }
        long stop = System.currentTimeMillis();

        long duration = stop - start;
        puts("new parse", duration, total);



        start = System.currentTimeMillis();
        total = 0;

        for (String num : numbers) {
            final long i = Long.parseLong(num);
            total += i;
        }
        stop = System.currentTimeMillis();

        duration = stop - start;


        puts("old parse", duration, total);

    }

    //@Test //JDK wins this one but it is close
    public void speedTestDoubleLong() {

        int numIter = 10_000_000;

        long BIG_NUM = Integer.MAX_VALUE;

        List<String> numbers =  new ArrayList<>(numIter);

        for (int index =0; index < numIter; index++) {
            numbers.add("" + ((BIG_NUM+ index) * 1.33d));
        }
        long total = 0;

        puts("Number generated", numbers.size(), BIG_NUM);

        for (String num : numbers) {
            final double i = StringScanner.parseDouble(num);
            total += i;
        }

        puts (total);
        total = 0;
        long start = System.currentTimeMillis();

        for (String num : numbers) {
            final double i = StringScanner.parseDouble(num);
            total += i;
        }
        long stop = System.currentTimeMillis();

        long duration = stop - start;
        puts("new parse", duration, total);



        start = System.currentTimeMillis();
        total = 0;

        for (String num : numbers) {
            final double i = Double.parseDouble(num);
            total += i;
        }
        stop = System.currentTimeMillis();

        duration = stop - start;


        puts("old parse", duration, total);

    }



    //@Test //We win this one
    public void speedTestDoubleLong2() {

        int numIter = 10_000_000;

        long BIG_NUM = 1_000_000;

        List<String> numbers =  new ArrayList<>(numIter);

        for (int index =0; index < numIter; index++) {
            numbers.add("" + ((BIG_NUM+ index) * 0.1d));
        }
        long total = 0;

        puts("Number generated", numbers.size(), BIG_NUM);

        for (String num : numbers) {
            final double i = StringScanner.parseDouble(num);
            total += i;
        }

        puts (total);
        total = 0;
        long start = System.currentTimeMillis();

        for (String num : numbers) {
            final double i = StringScanner.parseDouble(num);
            total += i;
        }
        long stop = System.currentTimeMillis();

        long duration = stop - start;
        puts("new parse", duration, total);



        start = System.currentTimeMillis();
        total = 0;

        for (String num : numbers) {
            final double i = Double.parseDouble(num);
            total += i;
        }
        stop = System.currentTimeMillis();

        duration = stop - start;


        puts("old parse", duration, total);

    }

    @Test
    public void parseFloatIssue179() {

        String testString = "-0.0";

        float value = StringScanner.parseFloat(testString);

        String str = ""+value;

        ok |= str.equals("-0.0") || die();
    }

    @Test
    public void testRemoveChars() {

        String testString = "1_2 345 6    _____\t\t7\t890";

        String after = StringScanner.removeChars ( testString, '_', ' ', '\t' );
        boolean ok =  "1234567890".equals (after ) || die( "$"+ after + "$");
    }

    @Test
    public void testCreateFromString() {

        String[] split = StringScanner.splitByCharsNoneEmpty( TEST_STRING, '[', ',', '"', '\\', ':', ']', '/' );

        String first = idx( split, 0 );
        String second = idx( split, 1 );
        String third = idx( split, 2 );
        String fourth = idx( split, 3 );
        String fifth = idx( split, 4 );
        String sixth = idx( split, 5 );
        String seventh = idx( split, 6 );

        String last = idx( split, -1 );


        assertEquals( "199984", first );


        assertEquals( "1384795052823", second );


        assertEquals( "127.0.0.1", third );


        assertEquals( "51706", fourth );


        assertEquals( "abc123", fifth );

        assertEquals( "rickHigh", sixth );
        assertEquals( "217.0.0.1", seventh );
        assertEquals( "asdfasdf", last );

        assertEquals( 12, len( split ) );


    }


    @Test
    public void testSimple() {

        String[] split = StringScanner.splitByCharsNoneEmpty( "1,2,3", ',' );

        String firstArg = idx( split, 0 );

        assertEquals( "1", firstArg );
    }

    @Test
    public void testSimple2() {

        String[] split = StringScanner.splitByCharsNoneEmpty( "1,2,,4", ',' );

        String firstArg = idx( split, 0 );
        String second = idx( split, 1 );
        String third = idx( split, 2 );

        assertEquals( "1", firstArg );

        assertEquals( "2", second );


        assertEquals( "4", third );
    }


    @Test
    public void testSubStringAfter() {
        final String substringAfter = StringScanner.substringAfter("love :: rocket", " :: ");

        ok |= substringAfter.equals("rocket") || die(substringAfter);

    }

    @Test
    public void testSubStringBefore() {
        final String substringBefore = StringScanner.substringBefore("love :: rocket", " :: ");

        ok |= substringBefore.equals("love") || die(substringBefore);

    }

}
