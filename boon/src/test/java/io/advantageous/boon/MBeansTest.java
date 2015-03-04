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

import io.advantageous.boon.MBeans;
import org.junit.Test;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Boon.toJson;
import static org.junit.Assert.assertEquals;


public class MBeansTest {

    public static interface HelloMBean {

        public void sayHello();

        public int add( int x, int y );

        public String getName();

    }


    public static class Hello implements HelloMBean {

        private String name = "value";

        public void sayHello() {
            System.out.println( "hello, world" );
        }

        public int add( int x, int y ) {
            return x + y;
        }

        public String getName() {
            return name;
        }
    }


    @Test
    public void test() throws Exception {


        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> objectNames = server.queryNames( null, null );

        for ( ObjectName name : objectNames ) {
            System.out.println( name.toString() );
            System.out.println( MBeans.map(server, name) );

        }

        //Set<ObjectInstance> instances = server.queryMBeans(null, null);


    }


    @Test
    public void jsonDump() throws Exception {
        puts(MBeans.toJson());
    }

    @Test
    public void createTest() throws Exception {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        Hello hello = new Hello();
        DynamicMBean dynamicMBean = MBeans.createMBean( hello, HelloMBean.class );

        MBeans.registerMBean( "com.example", "hello", dynamicMBean );
        Set<ObjectName> objectNames = server.queryNames( null, null );

        Map<String, Map<String, Object>> map = new LinkedHashMap<>();

        for ( ObjectName name : objectNames ) {

            map.put(name.toString(), MBeans.map(server, name));

        }

        puts("\n\n\n", toJson(map), "\n\n\n");

        puts();

        hello.name = "laskdjfal;ksdjf;laskjdf;laksjdfl;aksjdfl;kajsdf\n\n\n\n\\n\n";


        for ( ObjectName name : objectNames ) {
            System.out.println( name.toString() );
            System.out.println( MBeans.map( server, name ) );

        }


    }




}