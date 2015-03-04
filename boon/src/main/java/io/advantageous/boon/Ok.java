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

import java.util.Collection;
import java.util.Map;

import static io.advantageous.boon.Exceptions.die;

/**
 * Created by Richard on 3/7/14.
 */
public class Ok {

    public static boolean ok(Object object) {
        return object!=null;
    }


    public static boolean ok(boolean value) {
        return value;
    }

    public static boolean ok(Number i) {
        return i!=null && i.intValue()!=0;
    }

    public static boolean ok(int i) {
        return i!=0;
    }

    public static boolean ok(long i) {
        return i!=0;
    }

    public static boolean ok(Map map) {
        return map!=null && map.size() >0;
    }

    public static boolean ok(Collection c) {
        return c!=null && c.size() >0;
    }


    public static boolean ok(CharSequence cs) {
        return cs!=null && cs.length() >0;
    }




    public static boolean okOrDie(Object object) {
        return object!=null || Exceptions.die();
    }


    public static boolean okOrDie(boolean value) {
        return value || Exceptions.die();
    }

    public static boolean okOrDie(Number i) {
        return (i!=null && i.intValue() !=0) || Exceptions.die();
    }


    public static boolean okOrDie(int i) {
        return i!=0 || Exceptions.die();
    }

    public static boolean okOrDie(long i) {
        return i!=0 || Exceptions.die();
    }

    public static boolean okOrDie(Map map) {
        return (map!=null && map.size() >0) || Exceptions.die();
    }

    public static boolean okOrDie(Collection c) {
        return (c!=null && c.size() >0) || Exceptions.die();
    }


    public static boolean okOrDie(CharSequence cs) {
        return (cs!=null && cs.length() >0) || Exceptions.die();
    }


    public static boolean okOrDie(String message, Object object) {
        return object!=null || Exceptions.die(message);
    }

    public static boolean okOrDie(String message, int i) {
        return i!=0 || Exceptions.die(message);
    }

    public static boolean okOrDie(String message, long i) {
        return i!=0 || Exceptions.die(message);
    }

    public static boolean okOrDie(String message, Map map) {
        return (map!=null && map.size() >0) || Exceptions.die(message);
    }

    public static boolean okOrDie(String message, Collection c) {
        return (c!=null && c.size() >0) || Exceptions.die(message);
    }


    public static boolean okOrDie(String message, CharSequence cs) {
        return (cs!=null && cs.length() >0) || Exceptions.die(message);
    }


    public static boolean okOrDie(String message, boolean value) {
        return value || Exceptions.die(message);
    }

    public static boolean okOrDie(String message, Number i) {
        return (i!=null && i.intValue() !=0) || Exceptions.die(message);
    }


}
