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
import io.advantageous.boon.core.reflection.Mapper;
import io.advantageous.boon.core.reflection.MapperComplex;
import org.junit.Test;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 5/26/14.
 */
public class MapperConversionTest {

    Employee employee;

    Mapper mapper = new MapperComplex();

    Employee boss = new Employee(1);
    boolean ok;


    public static class Employee {

        private  int i = -555;
        private  String abc;

        Employee(int i) {
            this.i = i;
        }

        Employee(String abc) {
            this.abc = abc;
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
        employee = mapper.fromList(Lists.list("abc"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("abc") || die();
        ok = employee.i == -555 || die();


    }


    @Test
    public void testFromListWithStringArgConvertableToNumber() throws Exception {
        employee = mapper.fromList(Lists.list("1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }


    @Test
    public void testFromListWithInt() throws Exception {
        employee = mapper.fromList(Lists.list(1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die();

    }

    @Test
    public void testFromListWithIntWithBossFirst() throws Exception {
        employee = mapper.fromList(Lists.list(boss, 1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die();

    }


    @Test
    public void testFromListWithStringArgConvertableToNumberBossFirst() throws Exception {
        employee = mapper.fromList(Lists.list(boss, "1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }



    @Test
    public void testFromListWithIntWithBossFirstNull() throws Exception {
        employee = mapper.fromList(Lists.list(null, 1), Employee.class);
        ok = employee != null || die();
        ok = !"1".equals(employee.abc) || die();
        ok = employee.i == 1 || die();

    }


    @Test
    public void testFromListWithStringArgConvertableToNumberBossFirstNull() throws Exception {
        employee = mapper.fromList(Lists.list(null, "1"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == -555 || die();


    }



    @Test
    public void looseStringFirst() throws Exception {
        employee = mapper.fromList(Lists.list(null, "1", 2), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == 2 || die();


    }


    @Test
    public void looseIntFirst() throws Exception {
        employee = mapper.fromList(Lists.list(null, 1, "2"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("2") || die();
        ok = employee.i == 1 || die();


    }

    @Test
    public void looseTwoStrings() throws Exception {
        employee = mapper.fromList(Lists.list(null, "1", "2"), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die(employee.abc);
        ok = employee.i == 2 || die();


    }


    @Test
    public void looseTwoInts() throws Exception {
        employee = mapper.fromList(Lists.list(null, 1, 2), Employee.class);
        ok = employee != null || die();
        ok = employee.abc.equals("1") || die();
        ok = employee.i == 2 || die();
    }

}
