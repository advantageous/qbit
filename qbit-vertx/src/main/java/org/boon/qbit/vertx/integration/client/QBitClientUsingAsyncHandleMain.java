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

package org.boon.qbit.vertx.integration.client;

import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Boon;
import org.boon.core.Handler;
import org.boon.qbit.vertx.QBitClient;
import org.boon.qbit.vertx.integration.model.Employee;
import org.boon.qbit.vertx.integration.model.EmployeeManagerProxy;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

import java.util.List;

import static org.boon.Boon.puts;

/**
 * Created by Richard on 10/3/14.
 * @author Rick Hightower
 */
public class QBitClientUsingAsyncHandleMain {

    public static void main(String... args) throws InterruptedException {

        RegisterBoonWithQBit.registerBoonWithQBit();


        /* Create a new instance of Vertx. */
        Vertx vertx = VertxFactory.newVertx();

        /* Create new client. */
        final QBitClient qBitClient = new QBitClient("localhost", 8080, "/services", vertx);

        /* Start the async callback handler. */
        qBitClient.startReturnProcessing();

        /* Create an employee proxy using the client proxy interface with the handler. */
        final EmployeeManagerProxy remoteProxy = qBitClient.createProxy(EmployeeManagerProxy.class,
                "employeeService");


        /* Add an employee to the proxy, which calls the websocket. */
        remoteProxy.addEmployee(new Employee("Rick", "Hightower", 10, 1L));


        /* Call list employees. To get a list of employees. Use the
           Handler to get the list of employees async.
         */
        remoteProxy.list(new Handler<List<Employee>>() {
            @Override
            public void handle(List<Employee> employees) {
                puts(employees);

            }
        });

        Boon.gets();

    }


}
