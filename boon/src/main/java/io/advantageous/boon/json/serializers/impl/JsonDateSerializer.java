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

package io.advantageous.boon.json.serializers.impl;

import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.cache.Cache;
import io.advantageous.boon.cache.CacheType;
import io.advantageous.boon.cache.SimpleCache;
import io.advantageous.boon.core.Dates;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.serializers.DateSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rick on 1/4/14.
 */
public class JsonDateSerializer implements DateSerializer {

    private final Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
    private final Cache<Object, String> dateCache = new SimpleCache<>(200, CacheType.LRU);


    @Override
    public final void serializeDate( JsonSerializerInternal jsonSerializer, Date date, CharBuf builder ) {
        String string = dateCache.get ( date );
        if ( string == null) {
            CharBuf buf =  CharBuf.create ( Dates.JSON_TIME_LENGTH );
            Dates.jsonDateChars ( calendar, date, buf );
            string = buf.toString();
            dateCache.put ( date, string );

        }
        builder.addChars ( FastStringUtils.toCharArray( string ) );
    }
}
