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

package io.advantageous.qbit.service.impl.queuecallbacks;

import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import org.boon.core.reflection.AnnotationData;
import org.boon.core.reflection.ClassMeta;
import org.boon.core.reflection.MethodAccess;

/**
 * @author rhightower on 2/10/15.
 */
public class AnnotationDrivenQueueCallbackHandler implements QueueCallBackHandler {

    public static final String QUEUE_CALLBACK_ANNOTATION_NAME = "QueueCallback";
    private final Object service;
    private MethodAccess queueStartBatch;
    private MethodAccess queueInit;
    private MethodAccess queueEmpty;
    private MethodAccess queueLimit;
    private MethodAccess queueShutdown;
    private MethodAccess queueIdle;
    private ClassMeta<Class<?>> classMeta;

    public AnnotationDrivenQueueCallbackHandler(Object service) {


        classMeta = ( ClassMeta<Class<?>> ) ClassMeta.classMeta(service.getClass());

        this.service = service;

        final Iterable<MethodAccess> methods = classMeta.methods();

        for ( MethodAccess methodAccess : methods ) {
            if ( methodAccess.hasAnnotation(QUEUE_CALLBACK_ANNOTATION_NAME) ) {
                processAnnotationForMethod(methodAccess);
            }

        }

    }

    private void processAnnotationForMethod(final MethodAccess methodAccess) {
        final AnnotationData annotation = methodAccess.annotation(QUEUE_CALLBACK_ANNOTATION_NAME);
        final String value = annotation.getValues().get("value").toString();
        final QueueCallbackType queueCallbackType = QueueCallbackType.valueOf(value);

        switch ( queueCallbackType ) {
            case IDLE:
                queueIdle = methodAccess;
                break;
            case SHUTDOWN:
                queueShutdown = methodAccess;
                break;
            case LIMIT:
                queueLimit = methodAccess;
                break;
            case INIT:
                queueLimit = methodAccess;
                break;
            case START_BATCH:
                queueStartBatch = methodAccess;
                break;
            case EMPTY:
                queueEmpty = methodAccess;
                break;
            case DYNAMIC:
                switch ( methodAccess.name() ) {
                    case "queueIdle":
                        queueIdle = methodAccess;
                        break;
                    case "queueShutdown":
                        queueShutdown = methodAccess;
                        break;
                    case "queueLimit":
                        queueLimit = methodAccess;
                        break;
                    case "queueInit":
                        queueInit = methodAccess;
                        break;
                    case "queueStartBatch":
                        queueIdle = methodAccess;
                        break;
                    case "queueEmpty":
                        queueEmpty = methodAccess;
                        break;
                }
                break;

        }

    }

    @Override
    public void queueLimit() {
        if ( queueLimit != null ) {
            queueLimit.invoke(service);
        }


    }

    @Override
    public void queueEmpty() {
        if ( queueEmpty != null ) {
            queueEmpty.invoke(service);
        }

    }

    @Override
    public void queueInit() {
        if ( queueInit != null ) {
            queueInit.invoke(this.service);
        }
    }

    @Override
    public void queueIdle() {
        if ( queueIdle != null ) {
            queueIdle.invoke(service);
        }
    }

    @Override
    public void queueShutdown() {
        if ( queueShutdown != null ) {
            queueShutdown.invoke(service);
        }

    }

    @Override
    public void queueStartBatch() {
        if ( queueStartBatch != null ) {
            queueStartBatch.invoke(service);
        }

    }

}
