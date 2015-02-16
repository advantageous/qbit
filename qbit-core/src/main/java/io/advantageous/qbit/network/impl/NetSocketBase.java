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

package io.advantageous.qbit.network.impl;

import io.advantageous.qbit.network.NetSocket;
import io.advantageous.qbit.network.NetworkSender;
import org.boon.core.Sys;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by rhightower on 2/14/15.
 */
public class NetSocketBase implements NetSocket {

    private final String remoteAddress;
    private final String uri;
    private final boolean binary;
    private NetworkSender networkSender;
    private volatile boolean open;
    private Consumer<String> textMessageConsumer = text -> {
    };
    private Consumer<byte[]> binaryMessageConsumer = bytes -> {
    };
    private Consumer<Void> closeConsumer = aVoid -> {
    };
    private Consumer<Void> openConsumer = aVoid -> {
    };
    private Consumer<Exception> errorConsumer = error -> {

        LoggerFactory.getLogger(NetSocketBase.class)
                .error(error.getMessage(), error);
    };

    public NetSocketBase(String remoteAddress, String uri, boolean open, boolean binary,
                         NetworkSender networkSender) {
        this.remoteAddress = remoteAddress;
        this.uri = uri;
        this.open = open;
        this.binary = binary;
        this.networkSender = networkSender;
    }

    @Override
    public String remoteAddress() {
        return remoteAddress;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public boolean isBinary() {
        return binary;
    }

    @Override
    public void onTextMessage(String message) {
        this.textMessageConsumer.accept(message);
    }

    @Override
    public void onBinaryMessage(byte[] bytes) {
        this.binaryMessageConsumer.accept(bytes);
    }

    @Override
    public void onClose() {
        open = false;
        this.closeConsumer.accept(null);
    }

    @Override
    public void onOpen() {
        open = true;
        this.openConsumer.accept(null);
    }

    @Override
    public void onError(Exception exception) {
        open = false;
        errorConsumer.accept(exception);
    }

    @Override
    public void sendText(String text) {
        try {
            networkSender.sendText(text);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public void sendBinary(byte[] bytes) {
        try {
            networkSender.sendBytes(bytes);
        } catch (Exception ex) {
            onError(ex);
        }
    }

    @Override
    public boolean isClosed() {
        return !open;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void setTextMessageConsumer(Consumer<String> textMessageConsumer) {
        this.textMessageConsumer = textMessageConsumer;
    }

    @Override
    public void setBinaryMessageConsumer(Consumer<byte[]> binaryMessageConsumer) {
        this.binaryMessageConsumer = binaryMessageConsumer;
    }

    @Override
    public void setCloseConsumer(Consumer<Void> closeConsumer) {
        this.closeConsumer = closeConsumer;
    }

    @Override
    public void setOpenConsumer(Consumer<Void> openConsumer) {
        this.openConsumer = openConsumer;
    }

    @Override
    public void setErrorConsumer(Consumer<Exception> exceptionConsumer) {
        this.errorConsumer = exceptionConsumer;
    }

    @Override
    public void close() {
        try {
            networkSender.close();
        } catch (Exception ex) {
            onError(ex);
        }

    }

    @Override
    public void open() {
        try {
            networkSender.open(this);
        } catch (Exception ex) {
            onError(ex);
        }
    }


    @Override
    public void openAndWait() {

        open();
        /* Try to open for three seconds. */
        int count = 300;
        while (!open) {
            Sys.sleep(10);
            count--;
            if (count <= 0) {
                throw new IllegalStateException("Unable to open WebSocket connection");
            }
        }
    }

    public void setSender(NetworkSender networkSender) {
        this.networkSender = networkSender;
    }
}
