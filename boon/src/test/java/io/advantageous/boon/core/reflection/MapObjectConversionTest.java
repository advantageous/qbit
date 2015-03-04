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
import java.util.Currency;
import java.util.Date;
import java.util.Map;
import io.advantageous.boon.Lists;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.core.reflection.MapObjectConversion.*;

import io.advantageous.boon.core.Dates;

import java.util.LinkedHashMap;

/**
 * Created by Richard on 4/25/14.
 */
public class MapObjectConversionTest {


    Employee employee;

    Employee boss = new Employee(1);
    boolean ok;

    public static class Employee {

        private  int i = -555;
        private  String abc;
        private Date dob = Dates.getUSDate( 5, 25, 1980 );
        private Currency currency = Currency.getInstance("USD");
        private BigDecimal salary = new BigDecimal("100000.00");
        private Map<String, String> name;

        Employee(int i) {
            this.i = i;
        }

        Employee(String abc) {
            this.abc = abc;
            this.name = new LinkedHashMap<>();
            this.name.put("first", "Joe");
            this.name.put("last", "Smith");
        }



        Employee(Employee boss, String abc) {
            this.abc = abc;

        }

        Employee(Employee boss, int i) {
            this.i = i;
        }



        Employee(Employee boss, String abc, int i) {
            this.i = i;
            this.abc = abc;
        }

        Employee(Employee boss, int i, String abc) {
            this.i = i;
            this.abc = abc;
        }



        @Override
        public String toString() {
            return "Employee{" +
                    "i=" + i +
                    ", abc='" + abc + '\'' +
                    '}';
        }
    }

    @Test
    public void testFromListWithStringArg() throws Exception {
        employee = fromList(Lists.list("abc"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("abc") || die();
        ok = employee.i == -555 || die();


    }


    @Test
    public void testFromListWithStringArgConvertableToNumber() throws Exception {
        employee = fromList(Lists.list("1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }


    @Test
    public void testFromListWithInt() throws Exception {
        employee = fromList(Lists.list(1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die(employee.i);

    }

    @Test
    public void testFromListWithIntWithBossFirst() throws Exception {
        employee = fromList(Lists.list(boss, 1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die(employee.i);

    }


    @Test
    public void testFromListWithStringArgConvertableToNumberBossFirst() throws Exception {
        employee = fromList(Lists.list(boss, "1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }



    @Test
    public void testFromListWithIntWithBossFirstNull() throws Exception {
        employee = fromList(Lists.list(null, 1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die();

    }


    @Test
    public void testFromListWithStringArgConvertableToNumberBossFirstNull() throws Exception {
        employee = fromList(Lists.list(null, "1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }



    @Test
    public void looseStringFirst() throws Exception {
        employee = fromList(Lists.list(null, "1", 2), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == 2 || die();


    }


    @Test
    public void looseIntFirst() throws Exception {
        employee = fromList(Lists.list(null, 1, "2"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("2") || die();
        ok = employee.i == 1 || die();


    }

    @Test
    public void looseTwoStrings() throws Exception {
        employee = fromList(Lists.list(null, "1", "2"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die(employee.abc);
        ok = employee.i == 2 || die();


    }


    @Test
    public void looseTwoInts() throws Exception {
        employee = fromList(Lists.list(null, 1, 2), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == 2 || die();
    }

    @Test
    public void testToMap() {
        Employee emp = new Employee("ABC" );
        Map<String, Object> employeeMap = toMap(emp);
        ok = employeeMap != null || die();
        ok = employeeMap.get("abc").equals( "ABC" ) || die();
        ok = employeeMap.get("i").equals( -555 ) || die();
        ok = employeeMap.get("dob").toString().equals( Dates.getUSDate( 5, 25, 1980 ).toString() ) || die();
        ok = employeeMap.get("currency").equals( Currency.getInstance( "USD" ) ) || die();
        ok = employeeMap.get("salary").equals(new BigDecimal( "100000.00" ) ) || die();

        ok = employeeMap.get("name") != null || die();
        ok = employeeMap.get("name") instanceof Map || die();
        Map name = (Map)employeeMap.get("name");
        ok = name.get("first").equals("Joe") || die();
        ok = name.get("last").equals("Smith") || die();
    }

    @Test
    public void testFromMap() {
        Employee emp = new Employee("DEF" );
        
        Map<String, Object> employeeMap = toMapWithType(emp);
        Employee emp2 = (Employee)fromMap(employeeMap);
        
        ok = emp2 != null || die();
        ok = emp2.abc.equals( "DEF" ) || die();
        ok = emp2.i == -555 || die();
        ok = emp2.dob.toString().equals( Dates.getUSDate( 5, 25, 1980 ).toString() ) || die();
        ok = emp2.currency.equals( Currency.getInstance( "USD" ) ) || die();
        ok = emp2.salary.equals(new BigDecimal( "100000.00" ) ) || die();
        
    }

}
