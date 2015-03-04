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

import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.Value;
import io.advantageous.boon.core.value.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Lists.*;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Maps.idxMap;
import static io.advantageous.boon.Maps.map;
import static io.advantageous.boon.Str.lpad;
import static io.advantageous.boon.Str.rpad;
import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.toJson;

/**
 * Created by Richard on 5/26/14.
 */
public class MapperTest {


    boolean ok;

    public static Class<Employee> employee = Employee.class;

    Mapper mapper = new MapperComplex();

    public static class Employee {
        String name;
        int age;
        Employee boss;

        List<Employee> reports;
        String[] descriptions;
        List<String> props;

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
                    '}';
        }
    }

    @Test
    public void testMain() {
        MapConversionTest.main();
    }

    public static void main( String... args ) {


        Mapper mapper = new MapperComplex();
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
        puts( "Rick's Name",  Maps.idx(rickMap, "name"), "from", rickMap  );

        puts( "Boss Name",    idx( idxList( rickList,    2 ),      0 ) );
        puts( "Boss Name",    Maps.idx(idxMap(rickMap, "boss"), "name") );




        String rickJsonList = toJson( rickList );
        String rickJsonMap  = toJson( rickMap );
        puts( "Rick JSON List", rpad( rickJsonList, 60 ), "len", rickJsonList.length() );
        puts( "Rick JSON Map ", rpad( rickJsonMap,  60 ), "len", rickJsonMap.length()  );
        puts ( "LEFT PAD speedTestParseInt shows the difference");
        puts( "Rick JSON List", lpad( rickJsonList, 60 ), "len", rickJsonList.length() );
        puts( "Rick JSON Map ", lpad( rickJsonMap,  60 ), "len", rickJsonMap.length()  );

        /** Converting from List to objects */
        Employee rickEmployee =  mapper.fromList( rickList,  employee);
        puts (                   "Rick Employee From List      ", rickEmployee);

        /** Converting Maps to objects */
        rickEmployee =  mapper.fromMap( rickMap, employee );
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
        Employee emp = mapper.fromList( list( ( Object ) "Rick", 29 ), Employee.class );

        ok = emp != null || die();
        ok |= emp.name != null || die();
        ok |= emp.name.equals( "Rick" ) || die();

        ok |= emp.age == 29 || die();


    }

    @Test
    public void testNullCollectionFromValueMap() {
        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringTuple("props", null));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok |= emp.props == null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }

    @Test
    public void testNullArrayFromValueMap() {
        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringTuple("descriptions", null));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok |= emp.descriptions == null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }


    @Test
    public void testCollectionFromMap() {
        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringArrayTuple("props", "Rick is great"));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok = emp.props != null || die();
        ok = emp.props.size() == 1 || die();
        ok = emp.props.get(0).equals("Rick is great") || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }

    @Test
    public void testCollectionFromSingleValueMap() {
        mapper = new MapperComplex(true);

        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringTuple("props", "Rick is great"));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok = emp.props != null || die();
        ok = emp.props.size() == 1 || die();
        ok = emp.props.get(0).equals("Rick is great") || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }

    @Test
    public void testArrayFromMap() {
        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringArrayTuple("descriptions", "Rick is great"));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok = emp.descriptions != null || die();
        ok = emp.descriptions.length == 1 || die();
        ok = emp.descriptions[0].equals("Rick is great") || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }

    @Test
    public void testArrayFromSingleValueMap() {
        mapper = new MapperComplex(true);

        ValueMap valueMap = new ValueMapImpl();
        valueMap.add(stringTuple("descriptions", "Rick is great"));
        valueMap.add(stringTuple("name", "Rick"));
        Employee emp = mapper.fromValueMap(valueMap, Employee.class);

        ok = emp != null || die();
        ok = emp.descriptions != null || die();
        ok = emp.descriptions.length == 1 || die();
        ok = emp.descriptions[0].equals("Rick is great") || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();
    }

    private MapItemValue stringArrayTuple(String key, String... values) {
        List<CharSequenceValue>list = new ArrayList<>();
        for (String value : values) {
            list.add(new CharSequenceValue(false, TypeType.STRING, 0, value.length(), value.toCharArray(), false, false));
        }
        return new MapItemValue(new CharSequenceValue(false, TypeType.STRING, 0, key.length(), key.toCharArray(), false, false ), new ValueContainer(list, TypeType.LIST, false));
    }

    private MapItemValue stringTuple(String props, String value) {
        Value v;
        if(null==value) {
            v= ValueContainer.NULL;
        }else {
            v = new CharSequenceValue(false, TypeType.STRING, 0, value.length(), value.toCharArray(), false, false);
        }
        return new MapItemValue(new CharSequenceValue(false, TypeType.STRING, 0, props.length(), props.toCharArray(), false, false ), v);
    }


    @Test
    public void testBasicFromMaps() {
        Employee emp = mapper.fromMap( map( "name", ( Object ) "Rick", "age", 29 ), Employee.class );
        ok = emp != null || die();
        ok = emp.name != null || die();
        ok = emp.name.equals( "Rick" ) || die();

        ok = emp.age == 29 || die();
    }


    @Test
    public void testFromMapWithAList() {
        List<Object> list = Lists.list((Object) "Jason", 21);
        Employee emp = mapper.fromMap( Maps.map( "name",  "Rick", "age", 29, "boss", list ), Employee.class );
        ok = emp != null || die();
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
        ok = emp != null || die();
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
        Employee emp = mapper.fromList( Lists.list(  "Rick", 29, list ), Employee.class );

        ok = emp != null || die();
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

        ok = emp != null || die();
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

        Employee emp = mapper.fromList( list( "Rick", 29, boss, reports ), Employee.class );

        ok = emp != null || die();
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
