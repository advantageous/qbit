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

import io.advantageous.boon.core.Fn;
import io.advantageous.boon.core.Predicate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;



import static io.advantageous.boon.Lists.reduceBy;
import static io.advantageous.boon.primitive.Arry.reduceBy;
import static io.advantageous.boon.primitive.Int.reduceBy;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListsTest {

    boolean ok;

    static class Person {
        String name;
    }

    static class Employee {
        String name;
        int salary = 100;

        Employee(String name) {
            this.name = name;
        }


        Employee(int salary, String name) {
            this.salary = salary;
            this.name = name;
        }
    }


    static class HRObject {

        private final Employee employee;

        HRObject(Employee employee) {
            this.employee = employee;
        }

        public String name() {
            return employee.name;
        }
    }

    @Test
    public void testDeepCopy() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        List<Person> people = Lists.deepCopy(list, Person.class);

        ok = people.size() == 2 || die();

        ok = people.get(0).name.equals("Bob");

        ok = people.get(1).name.equals("Sally");

    }

    @Test
    public void testFilter0() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        list.addAll(Lists.list(new Employee("Rick"), new Employee("Joe")));

        List<Employee> filtered = Lists.filterBy(list, new Fn() {
            boolean t(Employee e) { return e.salary>150;}
        });

        boolean ok = filtered.size() == 2 || die();
        ok &= filtered.get(0).name.equals("Bob") || die();
        ok &= filtered.get(1).name.equals("Sally") || die();
    }


    @Test
    public void testFilter() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        list.addAll(Lists.list(new Employee("Rick"), new Employee("Joe")));

        List<Employee> filtered = Lists.filterBy(list, new Object() {
            boolean t(Employee e) { return e.salary>150;}
        });

        boolean ok = filtered.size() == 2 || die();
        ok &= filtered.get(0).name.equals("Bob") || die();
        ok &= filtered.get(1).name.equals("Sally") || die();
    }


    @Test
    public void testFilter2() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        list.addAll(Lists.list(new Employee("Rick"), new Employee("Joe")));

        List<Employee> filtered = Lists.filterBy(list, new Predicate<Employee>() {
            @Override
            public boolean test(Employee input) {
                return input.salary > 150;
            }
        });

        boolean ok = filtered.size() == 2 || die();
        ok &= filtered.get(0).name.equals("Bob") || die();
        ok &= filtered.get(1).name.equals("Sally") || die();
    }

    static boolean filterBySalary(Employee emp) {
        return emp.salary > 150;
    }



    @Test
    public void testFilter3() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        list.addAll(Lists.list(new Employee("Rick"), new Employee("Joe")));

        List<Employee> filtered = Lists.filterBy(list, ListsTest.class, "filterBySalary");

        boolean ok = filtered.size() == 2 || die();
        ok &= filtered.get(0).name.equals("Bob") || die();
        ok &= filtered.get(1).name.equals("Sally") || die();
    }

    boolean filterBySalaryMethod(Employee emp) {
        return emp.salary > 150;
    }

    @Test
    public void testFilter4() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        Lists.setListProperty(list, "salary", 200);
        list.addAll(Lists.list(new Employee("Rick"), new Employee("Joe")));

        List<Employee> filtered = Lists.filterBy(list, this, "filterBySalaryMethod");

        boolean ok = filtered.size() == 2 || die();
        ok &= filtered.get(0).name.equals("Bob") || die();
        ok &= filtered.get(1).name.equals("Sally") || die();
    }

    @Test
    public void testMapBy() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        List<HRObject> wrap = (List<HRObject>) Lists.mapBy(list, new Object() {
           HRObject hr(Employee e) {return new HRObject(e);}
        });

        boolean ok = wrap.get(0).name().equals("Bob") || die();

        ok &= wrap.get(1).name().equals("Sally") || die();

    }

    static HRObject createHRO(Employee e) {
        return new HRObject(e);
    }


    public static void main (String... args) {


        long start;
        long stop;
        List<HRObject> wrap;
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        ListsTest obj = new ListsTest();

        for (int index = 0; index < 2_000_000; index++) {

            list.add(new Employee("TEST" + index));
        }


        Object function = new Object() {
            final HRObject hr(Employee e) {return new HRObject(e);}
        };


        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, function);
            fakeCall(wrap);
        }

        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, ListsTest.class, "createHRO" );
            fakeCall(wrap);
        }

        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, obj, "createHROMethod" );
            fakeCall(wrap);
        }

        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, function);
            fakeCall(wrap);
        }







        start = System.currentTimeMillis();
        for (int index = 0; index < 100; index++) {
             wrap = (List<HRObject>) Lists.mapBy(list, ListsTest.class, "createHRO" );
            fakeCall(wrap);
        }
        stop = System.currentTimeMillis();
        puts ("Static reflection", (stop - start));


        start = System.currentTimeMillis();
        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, obj, "createHROMethod" );
            fakeCall(wrap);
        }
        stop = System.currentTimeMillis();
        puts ("reflection", (stop - start));


        start = System.currentTimeMillis();
        for (int index = 0; index < 100; index++) {
            wrap = Lists.wrap(HRObject.class, list );
            fakeCall(wrap);
        }
        stop = System.currentTimeMillis();
        puts ("wrap", (stop - start));


        start = System.currentTimeMillis();
        for (int index = 0; index < 100; index++) {
            wrap = new ArrayList(20);

            for (Employee employee : list) {

                wrap.add(new HRObject(employee));
            }
            fakeCall(wrap);
        }
        stop = System.currentTimeMillis();
        puts ("java for loop", (stop - start) );


        start = System.currentTimeMillis();
        for (int index = 0; index < 100; index++) {
            wrap = (List<HRObject>) Lists.mapBy(list, function);
            fakeCall(wrap);
        }
        stop = System.currentTimeMillis();
        puts ("anon reflection", (stop - start) );






    }

    private static void fakeCall(List<HRObject> wrap) {
        wrap.size();
    }


    HRObject createHROMethod(Employee e) {
        return new HRObject(e);
    }


    @Test
    public void reduce() {
      long sum =  (int) reduceBy(Lists.list(1,2,3,4,5,6,7,8), new Object() {
          int sum(int s, int b) {return s+b;}
      });

      boolean ok = sum == 36 || die();
      puts (sum);



      sum =  (long) reduceBy(new Integer[]{1,2,3,4,5,6,7,8}, new Object() {
            long sum(long s, int b) {return s+b;}
      });

      ok &= sum == 36 || die();






    }

    @Test
    public void testMapByStaticFunc() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        List<HRObject> wrap = (List<HRObject>) Lists.mapBy(list, ListsTest.class, "createHRO" );

        boolean ok = wrap.get(0).name().equals("Bob") || die();

        ok &= wrap.get(1).name().equals("Sally") || die();

    }


    @Test
    public void testMapByMethod() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        List<HRObject> wrap = (List<HRObject>) Lists.mapBy(list, this, "createHROMethod" );

        boolean ok = wrap.get(0).name().equals("Bob") || die();

        ok &= wrap.get(1).name().equals("Sally") || die();

    }



    @Test
    public void testWrapper() {
        List<Employee> list = Lists.list(new Employee("Bob"), new Employee("Sally"));
        List<HRObject> wrap = Lists.wrap(HRObject.class, list);

        boolean ok = wrap.get(0).name().equals("Bob") || die();

        ok &= wrap.get(1).name().equals("Sally") || die();
    }


    @Test
    public void iterAndFriends() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );

        iterAndFriends( list );

        list =
                safeList( "apple", "oranges", "pears", "grapes", "kiwi" );

        iterAndFriends( list );


        list =
                linkedList( "apple", "oranges", "pears", "grapes", "kiwi" );

        iterAndFriends( list );

        list =
                linkedList( list );

        iterAndFriends( list );

        list =
                safeList( list );

        iterAndFriends( list );


        list =
                copy( safeList( list ) );

        iterAndFriends( list );


        list =
                copy( linkedList( list ) );

        iterAndFriends( list );


        list =
                copy( list( list ) );

        iterAndFriends( list );


    }


    public void iterAndFriends( List<String> list ) {

        list = list( list.iterator() );
        assertEquals( 5, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );

        list = new Vector<>( list );
        list = list( list );
        assertEquals( 5, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );


        list = new Vector<>( list );
        list = list( ( Iterable<String> ) list );
        assertEquals( 5, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );

        list = list( enumeration( list ) );
        assertEquals( 5, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );

        list = list( String.class );
        list.add( "hi" );
        assertEquals( 1, len( list ) );
        assertTrue( in( "hi", list ) );


    }

    @Test
    public void sliceTest() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );
        List<String> list2 = slc( list, 0, 2 );

        assertEquals( list( "apple", "oranges" ), list2 );
    }


    @Test
    public void sliceTest2() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );
        List<String> list2 = slc( list, -3, -1 );

        assertEquals( list( "pears", "grapes" ), list2 );
    }


    @Test
    public void sliceStartNeg() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );

        List<String> list2;

        //listStream[-2:]
        //['grapes', 'kiwi']
        list2 = slc( list, -2 );

        assertEquals( list( "grapes", "kiwi" ), list2 );

    }


    @Test
    public void sliceStartPos() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );

        List<String> list2;


        //listStream[2 : ]
        //['pears', 'grapes', 'kiwi']
        list2 = slc( list, 2 );

        assertEquals( list( "pears", "grapes", "kiwi" ), list2 );


    }

    @Test
    public void sliceEnd() {
        List<String> list =
                list( "apple", "oranges", "pears", "grapes", "kiwi" );

        // listStream[: -3]
        // ['apple', 'oranges']

        List<String> list2 = slcEnd( list, -3 );

        assertEquals( list( "apple", "oranges" ), list2 );


        // listStream[: 2]
        // ['apple', 'oranges']
        list2 = slcEnd( list, 2 );

        assertEquals( list( "apple", "oranges" ), list2 );

    }

    @Test
    public void tooBigIndex() {
        List<String> list =
                list( "apple", "oranges", "pears" );
        idx( list, 100 );

        slc( list, -100 );

        slc( list, 100 );

    }


    @Test
    public void tooSmallIndex() {
        List<String> list =
                list( "apple", "oranges", "pears" );
        idx( list, -100 );

        slcEnd( list, -100 );

        slcEnd( list, 100 );

    }

    @Test
    public void testMe() {
        List<String> list = list( ( String[] ) null );
        System.out.println( list );

        list =
                list( "apple", "oranges", "pears" );
        assertEquals( 3, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );


        assertEquals( "pears", idx( list, -1 ) );
        assertEquals( "oranges", idx( list, -2 ) );
        assertEquals( "apple", idx( list, -3 ) );


        idx( list, 1, "grapes" );
        assertEquals( "grapes", idx( list, 1 ) );
        assertEquals( 3, len( list ) );


        idx( list, -2, "pear" );
        assertEquals( "pear", idx( list, 1 ) );
        assertEquals( 3, len( list ) );

        List<String> list2 = copy( list );
        assertEquals( 3, len( list2 ) );
        assertTrue( in( "apple", list2 ) );
        assertEquals( "pear", idx( list2, 1 ) );

        list2 = copy( ( ArrayList<String> ) list );
        assertEquals( 3, len( list2 ) );
        assertTrue( in( "apple", list2 ) );
        assertEquals( "pear", idx( list2, 1 ) );

        add( list, "berry" );

        assertEquals( "berry", idx( list, 3 ) );


    }

    @Test
    public void testSafe() {
        CopyOnWriteArrayList<String> list = ( CopyOnWriteArrayList<String> ) safeList( "apple", "oranges", "pears" );
        assertEquals( 3, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );
        idx( list, 1, "pear" );
        assertEquals( "pear", idx( list, 1 ) );
        assertEquals( 3, len( list ) );

        List<String> list2 = copy( list );
        assertEquals( 3, len( list2 ) );
        assertTrue( in( "apple", list2 ) );
        assertEquals( "pear", idx( list2, 1 ) );


    }


    @Test
    public void testLinked() {
        LinkedList<String> list = ( LinkedList<String> ) linkedList( "apple", "oranges", "pears" );
        assertEquals( 3, len( list ) );
        assertTrue( in( "apple", list ) );
        assertEquals( "oranges", idx( list, 1 ) );
        idx( list, 1, "pear" );
        assertEquals( "pear", idx( list, 1 ) );
        assertEquals( 3, len( list ) );

        List<String> list2 = copy( list );
        assertEquals( 3, len( list2 ) );
        assertTrue( in( "apple", list2 ) );
        assertEquals( "pear", idx( list2, 1 ) );

        List<String> list3 = copy( ( List<String> ) list );
        assertEquals( 3, len( list3 ) );
        assertTrue( in( "apple", list3 ) );
        assertEquals( "pear", idx( list3, 1 ) );


    }

}
