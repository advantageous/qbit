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

import io.advantageous.boon.core.reflection.fields.FieldAccess;
import io.advantageous.boon.json.serializers.JsonSerializerInternal;
import io.advantageous.boon.json.serializers.InstanceSerializer;
import io.advantageous.boon.primitive.CharBuf;

import java.util.Collection;
import java.util.Map;

/**
 * Created by rick on 1/1/14.
 */
public class InstanceSerializerImpl implements InstanceSerializer{

    @Override
    public final void serializeInstance ( JsonSerializerInternal serializer, Object instance, CharBuf builder ) {
        final Map<String, FieldAccess> fieldAccessors =   serializer.getFields(instance.getClass ());
        final Collection<FieldAccess> values = fieldAccessors.values ();



        builder.addChar( '{' );

        int index = 0;
        for ( FieldAccess fieldAccess : values ) {
            if (serializer.serializeField ( instance, fieldAccess, builder ) ) {
                builder.addChar ( ',' );
                index++;
            }
        }
        if ( index > 0 ) {
            builder.removeLastChar();
        }
        builder.addChar( '}' );

    }

    @Override
    public void serializeSubtypeInstance( JsonSerializerInternal serializer, Object instance, CharBuf builder ) {


        builder.addString( "{\"class\":" );
        builder.addQuoted ( instance.getClass ().getName () );
        final Map<String, FieldAccess> fieldAccessors = serializer.getFields ( instance.getClass () );

        int index = 0;
        Collection<FieldAccess> values = fieldAccessors.values();
        int length = values.size();

        if ( length > 0 ) {
            builder.addChar( ',' );


            for ( FieldAccess fieldAccess : values ) {
                boolean sent = serializer.serializeField ( instance, fieldAccess, builder );
                if (sent) {
                    index++;
                    builder.addChar( ',' );
                }
            }


            if ( index > 0 ) {
                builder.removeLastChar();
            }

            builder.addChar( '}' );


        }

    }

    @Override
    public void serializeInstance(JsonSerializerImpl serializer, Object instance, CharBuf builder, boolean includeTypeInfo) {

        final Map<String, FieldAccess> fieldAccessors =   serializer.getFields(instance.getClass ());
        final Collection<FieldAccess> values = fieldAccessors.values ();

        if (includeTypeInfo) {
            builder.addString("{\"class\":");
            builder.addQuoted(instance.getClass().getName());
            builder.addChar ( ',' );
        } else {

            builder.addChar('{');
        }
        int index = 0;
        for ( FieldAccess fieldAccess : values ) {
            if (serializer.serializeField ( instance, fieldAccess, builder ) ) {
                builder.addChar ( ',' );
                index++;
            }
        }
        if ( index > 0 ) {
            builder.removeLastChar();
        }
        builder.addChar( '}' );

    }
}
