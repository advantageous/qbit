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

package io.advantageous.boon.json.serializers;

import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by rick on 1/2/14.
 */
public interface JsonSerializerInternal extends JsonSerializer {

    CharBuf serialize ( Object obj );

    void serializeDate ( Date date, CharBuf builder );


    void serializeString ( String obj, CharBuf builder );

    void serializeCollection ( Collection<?> collection, CharBuf builder );

    void serializeMap ( Map<Object, Object> map, CharBuf builder );

    void serializeArray ( Object array, CharBuf builder );

    void serializeInstance ( Object obj, CharBuf builder );

    void serializeInstance ( Object obj, CharBuf builder, boolean includeTypeInfo );

    void serializeSubtypeInstance( Object obj, CharBuf builder );


    void serializeUnknown ( Object obj, CharBuf builder );

    void serializeObject ( Object value, CharBuf builder );

    Map<String, FieldAccess> getFields ( Class<? extends Object> aClass );

    boolean serializeField ( Object instance, FieldAccess fieldAccess, CharBuf builder );

}
