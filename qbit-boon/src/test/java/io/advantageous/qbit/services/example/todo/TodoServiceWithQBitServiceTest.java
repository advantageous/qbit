/*******************************************************************************

  * Copyright (c) 2015. Rick Hightower, Geoff Chandler
  *
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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.services.example.todo;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.spi.RegisterBoonWithQBit;
import org.boon.core.Sys;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/24/14.
 *
 * @author rhightower
 */
public class TodoServiceWithQBitServiceTest {

    boolean ok;

    static {

        /** Boon is the default implementation but there can be others. */
        RegisterBoonWithQBit.registerBoonWithQBit();
    }

    @Test
    public void testCallbackWithObjectNameAndMethodName() {


        Service service = QBit.factory().createService("/services", "/todo-service", new TodoService(), null, null).start();


        SendQueue<MethodCall<Object>> requests = service.requests();

        TodoItem todoItem = new TodoItem("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByNames("add", "/todo-service", "call1:localhost", todoItem, null);


        requests.send(addMethodCall);

        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByNames("list", "/todo-service", "call2:localhost", todoItem, null);

        requests.sendAndFlush(listMethodCall);

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = service.responses();

        Response<Object> response = responses.take();

        Object body = response.body();

        if ( body instanceof List ) {
            List<TodoItem> items = ( List ) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            TodoItem todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }
    }


    @Test
    public void testCallbackWithAddress() {


        Service service = QBit.factory().createService("/services", "/todo-service", new TodoService(), null, null).start();

        SendQueue<MethodCall<Object>> requests = service.requests();

        TodoItem todoItem = new TodoItem("call mom", "give mom a call", new Date());
        MethodCall<Object> addMethodCall = QBit.factory().createMethodCallByAddress("/services/todo-service/add", "call1:localhost", todoItem, null);


        requests.send(addMethodCall);

        MethodCall<Object> listMethodCall = QBit.factory().createMethodCallByAddress("/services/todo-service/list", "call2:localhost", todoItem, null);

        requests.sendAndFlush(listMethodCall);

        Sys.sleep(100);

        ReceiveQueue<Response<Object>> responses = service.responses();

        Response<Object> response = responses.take();

        Object body = response.body();

        if ( body instanceof List ) {
            List<TodoItem> items = ( List ) body;

            ok = items.size() > 0 || die("items should have one todo in it");

            TodoItem todoItem1 = items.get(0);

            ok = todoItem.equals(todoItem1) || die("TodoItem ", todoItem, todoItem1);


        } else {
            die("Response was not a list", body);
        }
    }
}
