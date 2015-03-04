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

package io.advantageous.boon.core.value;

import io.advantageous.boon.core.Value;

import java.util.*;

public class ValueList extends AbstractList<Object> implements List<Object> {

    List<Object> list = new ArrayList<>( 5 );

    private final boolean lazyChop;
    boolean converted = false;



    public ValueList( boolean lazyChop ) {
        this.lazyChop = lazyChop;
    }

    @Override
    public Object get( int index ) {

        Object obj = list.get( index );

        if ( obj instanceof Value ) {
            obj = convert( ( Value ) obj );
            list.set( index, obj );
        }

        chopIfNeeded( obj );
        return obj;

    }


    private Object convert( Value value ) {
        return value.toValue();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override

    public Iterator<Object> iterator() {
        convertAllIfNeeded();
        return list.iterator();
    }


    private void convertAllIfNeeded() {
        if ( !converted ) {
            converted = true;
            for ( int index = 0; index < list.size(); index++ ) {
                this.get( index );
            }
        }

    }


    @Override
    public void clear() {
        list.clear();
    }


    public boolean add( Object obj ) {
        return list.add( obj );
    }


    public void chopList() {

        for ( Object obj : list ) {
            if ( obj == null ) continue;

            if ( obj instanceof Value ) {
                Value value = ( Value ) obj;
                if ( value.isContainer() ) {
                    chopContainer( value );
                } else {
                    value.chop();
                }
            }
        }
    }

    private void chopIfNeeded( Object object ) {
        if ( lazyChop ) {
            if ( object instanceof LazyValueMap ) {
                LazyValueMap m = ( LazyValueMap ) object;
                m.chopMap();
            } else if ( object instanceof ValueList ) {
                ValueList list = ( ValueList ) object;
                list.chopList();
            }
        }

    }


    void chopContainer( Value value ) {
        Object obj = value.toValue();
        if ( obj instanceof LazyValueMap ) {
            LazyValueMap map = ( LazyValueMap ) obj;
            map.chopMap();
        } else if ( obj instanceof ValueList ) {
            ValueList list = ( ValueList ) obj;
            list.chopList();
        }
    }



    public ListIterator<Object> listIterator() {
        convertAllIfNeeded();
        return list.listIterator();
    }

    public List<Object> list () {
        return this.list;
    }
}
