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

package io.advantageous.boon.core;

import io.advantageous.boon.Lists;
import io.advantageous.boon.Maps;
import io.advantageous.boon.core.Conversions;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 2/25/14.
 */
public class ConversionsTest {


    static class Age {
        int i;
        Age(int i) {
            this.i = i;
        }
    }


    @Test
    public void convertAgeFromInt() {
        Age age = Conversions.coerce(Age.class, 11);

        boolean ok = age.i == 11 || die();
    }



    @Test
    public void convertAgeFromMap() {
        Map<String,Object> map = Maps.map("i", (Object)11);
        Age age = Conversions.coerce(Age.class, map);

        boolean ok = age.i == 11 || die();
    }



    @Test
    public void convertAgeFromList() {
        List list = Lists.list( 11);
        Age age = Conversions.coerce(Age.class, list);

        boolean ok = age.i == 11 || die();
    }




    static class Employee {
        String name;
        Age age;

        Employee(String name, Age age) {
            this.name = name;
            this.age = age;
        }


        Employee( Age age) {
            this.name = name;
            this.age = age;
        }
    }




    @Test
    public void convertEmployeeFromInt() {
        Employee e = Conversions.coerce(Employee.class, 11);

        boolean ok = e.age.i == 11 || die();


   }

    @Test
    public void convertEmployeeFromList() {
        Employee e = Conversions.coerce(Employee.class, Lists.list("Rick", 11));


        boolean ok = e.name.equals("Rick") || die();
        ok &= e.age.i == 11 || die();

    }


    @Test
    public void convertEmployeeFromListStringString() {
        Employee e = Conversions.coerce(Employee.class, Lists.list("Rick", "11"));


        boolean ok = e.name.equals("Rick") || die();
        ok &= e.age.i == 11 || die();

    }

    @Test
    public void convertEmployeeFromListList() {
        Employee e = Conversions.coerce(Employee.class, Lists.list("Rick", Lists.list(11)));


        boolean ok = e.name.equals("Rick") || die();
        ok &= e.age.i == 11 || die();

    }

    @Test
    public void convertEmployeeFromListStringNull() {
        Employee e = Conversions.coerce(Employee.class, Lists.list("Rick", null));


        boolean ok = e.name.equals("Rick") || die();
        ok &= e.age == null || die();

    }



    @Test
    public void convertEmployeeFromListNullNull() {
        Employee e = Conversions.coerce(Employee.class, Lists.list(null, null));


        boolean ok = e.name == null || die();
        ok &= e.age == null || die();

    }



    @Test
    public void convertEmployeeFromNull() {
        Employee e = Conversions.coerce(Employee.class, null);
        boolean ok = e != null || die();

    }



    @Test
    public void coerceInt() {
        int i = Conversions.coerce(int.class, 1);
        boolean ok = i == 1 || die();

    }



    @Test
    public void coerceNullToInt() {
        int i = Conversions.coerce(int.class, null);
        boolean ok = i == 0 || die();

    }


    @Test
    public void coerceNullBoolean() {
        boolean i = Conversions.coerce(boolean.class, null);
        boolean ok = !i || die();

    }





}
