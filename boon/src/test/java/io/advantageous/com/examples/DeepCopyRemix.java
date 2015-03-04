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

package io.advantageous.com.examples;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.Sets.deepCopy;
import static io.advantageous.boon.Sets.set;
import static io.advantageous.boon.Lists.list;
import static io.advantageous.boon.Lists.deepCopy;
import static io.advantageous.boon.Str.add;
import static io.advantageous.boon.core.reflection.BeanUtils.setCollectionProperty;
import static io.advantageous.boon.core.reflection.BeanUtils.copy;

public class DeepCopyRemix {



    public static class Employee {
        private String name;
        private String designation;

        public Employee(String name, String designation) {
            this.name = name;
            this.designation = designation;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Employee employee = (Employee) o;

            if (designation != null ? !designation.equals(employee.designation) : employee.designation != null)
                return false;
            if (name != null ? !name.equals(employee.name) : employee.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (designation != null ? designation.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return add(name, ":", designation);
        }
    }



    public static void main2 (String... args) {

        Set<Employee> original, copy;

        original = set(
                new Employee( "Joe", "Manager" ),
                new Employee( "Tim", "Developer" ),
                new Employee( "Frank", "Developer" ));

        copy = deepCopy( original );

        setCollectionProperty(original, "designation", "staff");

        original.iterator().next().setDesignation("staff");

        puts("Original Set after modification", original);

        puts("Copy of Set after modification",  copy);




        Employee employee = new Employee("Joe", "Manager");
        Employee copyEmployee = copy(employee);


        if (employee == copyEmployee) {
            die("not same object");
        }

        if (copy.iterator().next() == original.iterator().next()) {
            die("not same object");
        };
    }


    public static void main (String... args) {
        main1();
        main2();
    }

    public static void main1 (String... args) {

        List<Employee> original, copy;

        original = list(
                new Employee("Joe", "Manager"),
                new Employee("Tim", "Developer"),
                new Employee("Frank", "Developer"));

        copy = deepCopy( original );

        setCollectionProperty(original, "designation", "staff");

        original.iterator().next().setDesignation("staff");

        puts("Original List after modification", original);

        puts("Copy of List after modification",  copy);

        if (copy.iterator().next() == original.iterator().next()) {
            die("not same object");
        }


    }


    @Test
    public void test() {

        DeepCopyRemix.main2();
        DeepCopyRemix.main();


    }


}
