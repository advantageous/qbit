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

package io.advantageous.com.examples.model.test.movies.entitlement;


import io.advantageous.com.examples.model.test.time.DateTime;
import io.advantageous.com.examples.model.test.time.TimeZoneHolder;
import io.advantageous.com.examples.model.test.time.TimeZoneType;

import java.util.HashSet;
import java.util.Set;

/**
 * Watcher Rights. Their rights. Their subscription.
 * @author Rick Hightower
 */
public class Rights {

    private DateTime expire;
    private Set<RightsType> types;
    private boolean active;


    public static Rights createRights(RightsType type, boolean active, TimeZoneType timeZoneType, long milis) {
        Rights rights = new Rights(type, active, new DateTime(new TimeZoneHolder(timeZoneType), milis));
        return rights;
    }

    public Rights(RightsType type, boolean active, DateTime expire) {
        types = new HashSet<>();
        types.add(type);
        this.active = active;
        this.expire = expire;
    }


    public Rights(RightsType type, boolean active) {
        types = new HashSet<>();
        types.add(type);
        this.active = active;

    }


    public Rights(RightsType type) {
        types = new HashSet<>();
        types.add(type);
    }


    public Set<RightsType> getTypes() {
        return types;
    }

    public void setTypes(Set<RightsType> types) {
        this.types = types;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public DateTime getExpire() {
        return expire;
    }

    public void setExpire(DateTime expire) {
        this.expire = expire;
    }

    @Override
    public String toString() {
        return "Rights{" +
                "types=" + types +
                ", active=" + active +
                ", expire=" + expire +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rights)) return false;

        Rights that = (Rights) o;

        if (active != that.active) return false;
        if (expire != null ? !expire.equals(that.expire) : that.expire != null) return false;
        if (types != null ? !types.equals(that.types) : that.types != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = types != null ? types.hashCode() : 0;
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (expire != null ? expire.hashCode() : 0);
        return result;
    }
}
