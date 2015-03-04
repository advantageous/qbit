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

import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.json.serializers.MapSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Map;
import java.util.Set;

/**
 * Created by rick on 1/1/14.
 */
public class MapSerializerImpl implements MapSerializer {
    private static final char [] EMPTY_MAP_CHARS = {'{', '}'};
    private final boolean includeNulls;

    public MapSerializerImpl(boolean includeNulls) {
        this.includeNulls = includeNulls;
    }


    private void serializeFieldName ( String name, CharBuf builder ) {
        builder.addJsonFieldName ( FastStringUtils.toCharArray(name) );
    }

    @Override
    public final void serializeMap ( JsonSerializerInternal serializer, Map<Object, Object> map, CharBuf builder ) {

        if ( map.size () == 0 ) {
            builder.addChars ( EMPTY_MAP_CHARS );
            return;
        }


        builder.addChar( '{' );

        final Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        int index=0;

        if (!includeNulls) {
            for (Map.Entry<Object, Object> entry : entrySet) {
                if (entry.getValue() != null) {
                    serializeFieldName(entry.getKey().toString(), builder);
                    serializer.serializeObject(entry.getValue(), builder);
                    builder.addChar(',');
                    index++;
                }
            }
        }else {
            for (Map.Entry<Object, Object> entry : entrySet) {
                    serializeFieldName(entry.getKey().toString(), builder);
                    serializer.serializeObject(entry.getValue(), builder);
                    builder.addChar(',');
                    index++;

            }
        }
        if (index>0)
        builder.removeLastChar ();
        builder.addChar( '}' );

    }
}
