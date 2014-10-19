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

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.Boon;
import org.boon.core.Sys;
import org.boon.core.reflection.MapObjectConversion;
import org.boon.qbit.vertx.QBitClient;
import org.boon.qbit.vertx.integration.model.Employee;
import org.boon.qbit.vertx.integration.model.EmployeeManager;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

import java.util.List;
import java.util.Map;

import static org.boon.Boon.puts;

/**
 * Created by Richard on 10/2/14.
 * @author Rick Hightower
 */
public class QBitClientMain {



    public static void main (String... args) throws InterruptedException {


        RegisterBoonWithQBit.registerBoonWithQBit();

        /* Create a new instance of Vertx. */
        Vertx vertx = VertxFactory.newVertx();


        final QBitClient qBitClient = new QBitClient("localhost", 8080, "/services", vertx);


        /** You can use the regular interface but then you have to
         * Use a return queue.
         */
        final EmployeeManager remoteProxy = qBitClient.createProxy(EmployeeManager.class,
                "employeeService");


        remoteProxy.addEmployee(new Employee("Rick", "Hightower", 10, 1L));


        /* Call list but ignore the return type because it comes back async
         * in this example anyway.
         */
        remoteProxy.list();

        /* Receive queue. */
        final ReceiveQueue<String> receiveQueue = qBitClient.receiveQueue();

        Sys.sleep(1000);


        /* This is a raw message from websocket so we will need to parse it. */
        final String message = receiveQueue.pollWait();

        puts(message);

        /* This is how we parse it. */
        final Response<Object> response = QBit.factory().createResponse(message);

        /* Now parse the employees object. */
        final List<Employee> employees = MapObjectConversion.convertListOfMapsToObjects(Employee.class, (List<Map>) response.body());


        puts(employees);

        Boon.gets();

    }




}
