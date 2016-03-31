/**
 * ****************************************************************************
 * <p>
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ________ __________.______________
 * \_____  \\______   \   \__    ___/
 * /  / \  \|    |  _/   | |    |  ______
 * /   \_/.  \    |   \   | |    | /_____/
 * \_____\ \_/______  /___| |____|
 * \__>      \/
 * ___________.__                  ____.                        _____  .__                                             .__
 * \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
 * |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
 * |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
 * |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
 * \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
 * .____    ._____.
 * |    |   |__\_ |__
 * |    |   |  || __ \
 * |    |___|  || \_\ \
 * |_______ \__||___  /
 * \/       \/
 * ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
 * |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
 * |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
 * /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
 * \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
 * \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
 * __________           __  .__              __      __      ___.
 * \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
 * |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
 * |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
 * |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
 * \/     \/             \/     \/         \/       \/    \/
 * <p>
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * <p>
 * ****************************************************************************
 */

package io.advantageous.qbit.service;

/**
 * A service that can be flushed.
 * created by rhightower on 2/18/15.
 */
public interface ServiceFlushable {


    default void flush() {
    }
}
