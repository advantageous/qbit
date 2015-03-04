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


import io.advantageous.boon.Lists;

import java.util.List;
import java.util.TimeZone;

import static io.advantageous.boon.Exceptions.die;

/**
 */
public class TimeZoneHolder {

    public transient static final TimeZoneHolder PST = new TimeZoneHolder(TimeZoneType.PST);
    private transient TimeZone tz;
    private TimeZoneType type;
    private String other;

    public TimeZoneHolder( TimeZoneType type, String str ) {
        this.type = type;
        this.other = str;
     }

    public TimeZoneHolder( TimeZoneType type ) {
        this.type = type;
    }



    public TimeZone timeZone() {
        if (tz == null) {
            switch (type) {
                case OTHER_AFRICA:
                case OTHER_ASIA:
                case OTHER:
                case OTHER_EASTERN_EUROPE:
                case OTHER_UK:
                case OTHER_WESTERN_EUROPE:
                case OTHER_ASIA_PACIFIC:
                case OTHER_CANADA:
                    tz = TimeZone.getTimeZone( other );
                    break;
                default:
                    tz = type.timeZone;
            }
        }
        return tz;
    }

    public TimeZoneType type() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeZoneHolder that = (TimeZoneHolder) o;

        if (other != null ? !other.equals(that.other) : that.other != null) return false;
        if (type != that.type) return false;
        if (tz != null ? !tz.equals(that.tz) : that.tz != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tz != null ? tz.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (other != null ? other.hashCode() : 0);
        return result;
    }

    public List<Object> toList() {

        if (other!=null) {
            return Lists.list((Object)this.type, this.other);
        } else {
            return Lists.list((Object)this.type);
        }
    }

    @Override
    public String toString() {
        return "TimeZoneHolder{" +
                "tz=" + tz +
                ", type=" + type +
                ", other='" + other + '\'' +
                '}';
    }
}
