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

package io.advantageous.boon.core.reflection;

import java.math.BigDecimal;
import java.util.*;
import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import org.junit.Test;



import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Lists.*;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Maps.*;
import static io.advantageous.boon.json.JsonFactory.*;


import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Str.lpad;
import static io.advantageous.boon.Str.rpad;
import io.advantageous.boon.core.Dates;

/**
 * Created by Richard on 2/12/14.
 */
public class MapConversionTest {

    public static Class<Employee> employee = Employee.class;

    public static class Employee {
        String name;
        int age;
        Employee boss;

        List<Employee> reports;

        Date dob = Dates.getUSDate( 5, 25, 1980 );
        Currency currency = Currency.getInstance("USD");
        BigDecimal salary = new BigDecimal("100000.00");

        public Employee( String name, int age ) {
            this.name = name;
            this.age = age;
        }

        public Employee( String name, int age, Employee boss ) {
            this.name = name;
            this.age = age;
            this.boss = boss;
        }


        public Employee( String name, int age, Employee boss, List<Employee> reports ) {
            this.name = name;
            this.age = age;
            this.boss = boss;
            this.reports = reports;
        }

        @Override
        public String toString() {
            return "Employee{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", boss=" + (boss == null ? "executive" : boss) +
//                    ", reports=" + reports +
                    ", dob=" + dob +
                    ", currency=" + currency +
                    ", salary=" + salary +
                    '}';
        }
    }

    @Test
    public void testMain() {
        MapConversionTest.main();
    }
    
    public static void main( String... args ) {
        List<Object> rickList;
        Map<String, Object> rickMap;


        /** Creating a list. */
        rickList = list( "Rick", 29, list( "Jason", 21 ) );
        /** Creating a map. */
        rickMap = map(
                "name", "Rick",
                "age", 29,
                "boss", map( "name", "Jason", "age", 21 ) );

        /** Showing the list and map. */
        puts( "Rick List", rickList );
        puts( "Rick Map ", rickMap  );



        /** Showing the list and map with type info. */
        puts( "Rick List    ", rpad(rickList, 50), rickList.getClass() );
        puts( "Rick Map     ", rpad(rickMap,  50),  rickMap.getClass() );


        /** Indexing the list and map with boon slice index operator. */
        puts( "Rick's Name",  idx( rickList,     0 ), "from", rickList );
        puts( "Rick's Name",  idx( rickMap, "name" ), "from", rickMap  );

        puts( "Boss Name",    idx( idxList( rickList,    2 ),      0 ) );
        puts( "Boss Name",    idx( idxMap( rickMap, "boss" ), "name" ) );




        String rickJsonList = toJson( rickList );
        String rickJsonMap  = toJson( rickMap );
        puts( "Rick JSON List", rpad( rickJsonList, 60 ), "len", rickJsonList.length() );
        puts( "Rick JSON Map ", rpad( rickJsonMap,  60 ), "len", rickJsonMap.length()  );
        puts ( "LEFT PAD speedTestParseInt shows the difference");
        puts( "Rick JSON List", lpad( rickJsonList, 60 ), "len", rickJsonList.length() );
        puts( "Rick JSON Map ", lpad( rickJsonMap,  60 ), "len", rickJsonMap.length()  );

        /** Converting from List to objects */
        Employee rickEmployee =  fromList( rickList,  employee);
        puts (                   "Rick Employee From List      ", rickEmployee);

        /** Converting Maps to objects */
        rickEmployee =  fromMap( rickMap, employee );
        puts (                   "Rick Employee From Map       ", rickEmployee);



        /** Converting from JSON List to objects */
        rickEmployee =  fromJson( rickJsonList, employee );
        puts (                   "Rick Employee From JSON LIST ", rickEmployee);

        /** Converting from JSON Map to objects */
        rickEmployee =  fromJson( rickJsonList,  employee);
        puts (                   "Rick Employee From JSON LIST ", rickEmployee);
 
    }

    @Test
    public void testBasicFromList() {
        Employee emp = fromList( list( ( Object ) "Rick", 29 ), Employee.class );

        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();

    }


    @Test
    public void testBasicFromMaps() {
        Employee emp = fromMap( map( "name", ( Object ) "Rick", "age", 29 ), Employee.class );
        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();
    }


    @Test
    public void testFromMapWithAList() {
        List<Object> list = Lists.list( ( Object ) "Jason", 21 );
        Employee emp = fromMap( Maps.map( "name", ( Object ) "Rick", "age", 29, "boss", list ), Employee.class );
        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
        ok = emp.age == 29 || die();

        ok = emp.boss != null || die();
        ok = emp.boss.name.equals( "Jason" ) || die();
        ok = emp.boss.age == 21 || die();

    }



    @Test
    public void testFromJsonMapWithAList() {
        List<Object> list = Lists.list( ( Object ) "Jason", 21 );
        Employee emp = fromJson( toJson( map ( "name",  "Rick", "age", 29, "boss", list )), Employee.class );
        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
        ok = emp.age == 29 || die();

        ok = emp.boss != null || die();
        ok = emp.boss.name.equals( "Jason" ) || die();
        ok = emp.boss.age == 21 || die();

    }


    @Test
    public void testListWithSubList() {

        List<Object> list = Lists.list( ( Object ) "Jason", 21 );
        Employee emp = fromList( Lists.list( ( Object ) "Rick", 29, list ), Employee.class );

        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();

        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
        ok = emp.age == 29 || die();

        ok = emp.boss != null || die();
        ok = emp.boss.name.equals( "Jason" ) || die();
        ok = emp.boss.age == 21 || die();

    }




    @Test
    public void testListWithSubListWithSubListUsingJson() {

        List<Object> boss = list( ( Object ) "Jason", 21 );
        List<Object> report = list( ( Object ) "Lucas", 10 );


        List<Object> reports = new ArrayList<>();
        reports.add( report );

        String str = toJson(list( "Rick", 29, boss, reports));

        Employee emp = fromJson( str, Employee.class );

        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();

        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
        ok = emp.age == 29 || die();

        ok = emp.boss != null || die();
        ok = emp.boss.name.equals( "Jason" ) || die();
        ok = emp.boss.age == 21 || die();


        ok = emp.reports != null || die();

        ok = emp.reports.size() == 1 || die();
        ok = emp.reports.get( 0 ).name.equals( "Lucas" ) || die();
        ok = emp.reports.get( 0 ).age == 10 || die();

    }

    @Test
    public void testListWithSubListWithSubList() {

        List<Object> boss = list( ( Object ) "Jason", 21 );
        List<Object> report = list( ( Object ) "Lucas", 10 );


        List<Object> reports = new ArrayList<>();
        reports.add( report );

        Employee emp = fromList( list( "Rick", 29, boss, reports ), Employee.class );

        boolean ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();

        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
        ok = emp.age == 29 || die();

        ok = emp.boss != null || die();
        ok = emp.boss.name.equals( "Jason" ) || die();
        ok = emp.boss.age == 21 || die();


        ok = emp.reports != null || die();

        ok = emp.reports.size() == 1 || die();
        ok = emp.reports.get( 0 ).name.equals( "Lucas" ) || die();
        ok = emp.reports.get( 0 ).age == 10 || die();

    }

}
