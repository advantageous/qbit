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

package io.advantageous.com.examples.model.test.time;

import io.advantageous.boon.Str;
import io.advantageous.boon.core.Conversions;

import java.util.TimeZone;

/**
 */
public enum TimeZoneType {
    EST (TimeZone.getTimeZone( "EST" )),//Eastern
    PST (TimeZone.getTimeZone( "PST" )),//Pacific
    MST (TimeZone.getTimeZone( "MST" )),//Mountain
    HST (TimeZone.getTimeZone( "HST" )),//Hawaii
    AKST (TimeZone.getTimeZone( "AKST" )),//Alaska
    OTHER_ASIA_PACIFIC (null),
    OTHER_ASIA (null),
    OTHER_AFRICA (null),
    OTHER_EASTERN_EUROPE (null),
    OTHER_WESTERN_EUROPE (null),
    OTHER_CANADA (null),
    OTHER_UK (null),
    OTHER (null);

    final TimeZone timeZone;
    TimeZoneType( TimeZone timeZone ) {

        this.timeZone = timeZone;
    }

    public TimeZone timeZone() {
        return timeZone;
    }

    public static TimeZoneHolder createHolder(String timeZone) {
        TimeZoneType timeZoneType = Conversions.toEnum(TimeZoneType.class, timeZone);
        if (timeZoneType !=null) {
            return new TimeZoneHolder(timeZoneType);
        }
        else {
            String[] split = Str.split(timeZone, '.');
            if (split.length !=2) {
                return new TimeZoneHolder(OTHER);
            } else {
                timeZoneType = Conversions.toEnum(TimeZoneType.class, split[0]);
                if (timeZoneType == null) {
                    return new TimeZoneHolder(OTHER);
                } else {
                    return new TimeZoneHolder(timeZoneType, split[1]);
                }

            }
        }
    }
}
