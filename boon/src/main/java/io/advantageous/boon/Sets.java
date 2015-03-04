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


import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import io.advantageous.boon.primitive.CharBuf;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

public class Sets {

    /* Creation */
    public static <V> Set<V> set( Collection<V> collection ) {
        if (collection instanceof Set) {
            return (Set <V>) collection;
        }
        if (collection==null) {
            return Collections.EMPTY_SET;
        }
        return new LinkedHashSet<>( collection );
    }


    public static <V> Enumeration<V> enumeration( final Set<V> set ) {
        final Iterator<V> iter = set.iterator();
        return new Enumeration<V>() {
            @Override
            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            @Override
            public V nextElement() {
                return iter.next();
            }
        };

    }


    public static <V> Set<V> set( Class<V> clazz ) {
        return new LinkedHashSet<>();
    }

    public static <V> Set<V> set( Iterable<V> iterable ) {
        Set<V> set = new LinkedHashSet<>();
        for ( V o : iterable ) {
            set.add( o );
        }
        return set;
    }

    public static <V> Set<V> set( Enumeration<V> enumeration ) {
        Set<V> set = new LinkedHashSet<>();
        while ( enumeration.hasMoreElements() ) {
            set.add( enumeration.nextElement() );
        }
        return set;
    }


    public static <V> Set<V> set( Iterator<V> iterator ) {
        Set<V> set = new LinkedHashSet<>();
        while ( iterator.hasNext() ) {
            set.add( iterator.next() );
        }
        return set;
    }


    @SafeVarargs
    public static <V> Set<V> set( final V... array ) {


        Set<V> set = new LinkedHashSet<>();

        for ( V v : array ) {
            set.add( v );
        }
        return set;
    }

    @SafeVarargs
    public static <V> Set<V> set( int size, final V... array  ) {

        int index=0;
        Set<V> set = new LinkedHashSet<>();

        for ( V v : array ) {
            set.add( v );
            index++;
            if (index == size) {
                break;
            }
        }
        return set;
    }


    public static <V> NavigableSet<V> sortedSet( Iterator<V> iterator ) {
        NavigableSet<V> set = new TreeSet<>();
        while ( iterator.hasNext() ) {
            set.add( iterator.next() );
        }
        return set;
    }

    public static <V> NavigableSet<V> sortedSet( Class<V> clazz ) {
        return new TreeSet<>();
    }

    public static <V> NavigableSet<V> sortedSet( Iterable<V> iterable ) {
        NavigableSet<V> set = new TreeSet<>();
        for ( V o : iterable ) {
            set.add( o );
        }
        return set;
    }

    public static <V> NavigableSet<V> sortedSet( Enumeration<V> enumeration ) {
        NavigableSet<V> set = new TreeSet<>();
        while ( enumeration.hasMoreElements() ) {
            set.add( enumeration.nextElement() );
        }
        return set;
    }

    @SafeVarargs
    public static <V> NavigableSet<V> sortedSet( final V... array ) {
        NavigableSet<V> set = new TreeSet<>();

        for ( V v : array ) {
            set.add( v );
        }
        return set;
    }

    public static <V> NavigableSet<V> sortedSet( Collection<V> collection ) {
        return new TreeSet<>( collection );
    }


    public static <V> NavigableSet<V> safeSortedSet( Iterator<V> iterator ) {
        NavigableSet<V> set = new ConcurrentSkipListSet<>();
        while ( iterator.hasNext() ) {
            set.add( iterator.next() );
        }
        return set;
    }

    public static <V> NavigableSet<V> safeSortedSet( Class<V> clazz ) {
        return new ConcurrentSkipListSet<>();
    }

    public static <V> NavigableSet<V> safeSortedSet( Iterable<V> iterable ) {
        NavigableSet<V> set = new ConcurrentSkipListSet<>();
        for ( V o : iterable ) {
            set.add( o );
        }
        return set;
    }

    public static <V> NavigableSet<V> safeSortedSet( Enumeration<V> enumeration ) {
        NavigableSet<V> set = new ConcurrentSkipListSet<>();
        while ( enumeration.hasMoreElements() ) {
            set.add( enumeration.nextElement() );
        }
        return set;
    }

    @SafeVarargs
    public static <V> NavigableSet<V> safeSortedSet( final V... array ) {

        NavigableSet<V> set = new ConcurrentSkipListSet<>();

        for ( V v : array ) {
            set.add( v );
        }
        return set;

    }


    public static <V> NavigableSet<V> safeSortedSet( Collection<V> collection ) {
        return new ConcurrentSkipListSet<>( collection );
    }

    public static <V> Set<V> safeSet( Class<V> clazz ) {
        return new CopyOnWriteArraySet<>();
    }

    public static <V> Set<V> safeSet( Iterable<V> iterable ) {
        Set<V> set = new CopyOnWriteArraySet<>();
        for ( V o : iterable ) {
            set.add( o );
        }
        return set;
    }

    public static <V> Set<V> safeSet( Enumeration<V> enumeration ) {
        Set<V> set = new CopyOnWriteArraySet<>();
        while ( enumeration.hasMoreElements() ) {
            set.add( enumeration.nextElement() );
        }
        return set;
    }


    public static <V> Set<V> safeSet( Iterator<V> iterator ) {
        Set<V> set = new CopyOnWriteArraySet<>();
        while ( iterator.hasNext() ) {
            set.add( iterator.next() );
        }
        return set;
    }


    @SafeVarargs
    public static <V> Set<V> safeSet( final V... array ) {

        List<V> list = Lists.list( array );
        Set<V> set = new CopyOnWriteArraySet<>( list );

        return set;
    }

    public static <V> Set<V> safeSet( Collection<V> collection ) {
        return new CopyOnWriteArraySet<>( collection );
    }


    @Universal
    public static int len( Set<?> set ) {
        return set.size();
    }

    @Universal
    public static <V> boolean in( V value, Set<?> set ) {
        return set.contains( value );
    }

    @Universal
    public static <V> void add( Set<V> set, V value ) {
        set.add( value );
    }

    @Universal
    public static <T> T idx( NavigableSet<T> set, final T index ) {

        return set.higher( index );
    }

    @Universal
    public static <T> T idx( Set<T> set, final T index ) {

        if ( set instanceof NavigableSet ) {
            return idx( ( NavigableSet<T> ) set, index );
        } else {
            throw new IllegalArgumentException( "Set must be a NavigableSet for idx operation to work" );
        }
    }

    public static <T> T after( NavigableSet<T> set, final T index ) {

        return set.higher( index );
    }

    public static <T> T before( NavigableSet<T> set, final T index ) {

        return set.lower( index );
    }

    @Universal
    public static <V> SortedSet<V> slc( NavigableSet<V> set, V startIndex, V endIndex ) {
        return set.subSet( startIndex, endIndex );
    }


    @Universal
    public static <V> SortedSet<V> slcEnd( NavigableSet<V> set, V fromIndex ) {
        return set.tailSet( fromIndex );
    }


    @Universal
    public static <V> SortedSet<V> slc( NavigableSet<V> set, V toIndex ) {
        return set.headSet( toIndex );
    }

    @Universal
    public static <V> Set<V> copy( HashSet<V> collection ) {
        return new LinkedHashSet<>( collection );
    }

    @Universal
    public static <V> NavigableSet<V> copy( TreeSet<V> collection ) {
        return new TreeSet<>( collection );
    }

    @Universal
    public static <V> Set<V> copy( CopyOnWriteArraySet<V> collection ) {
        return new CopyOnWriteArraySet<>( collection );
    }

    @Universal
    public static <V> NavigableSet<V> copy( ConcurrentSkipListSet<V> collection ) {
        return new ConcurrentSkipListSet<>( collection );
    }


    @Universal
    public static <V> NavigableSet<V> copy( NavigableSet<V> collection ) {
        if ( collection instanceof ConcurrentSkipListSet ) {
            return copy( ( ConcurrentSkipListSet<V> ) collection );
        } else {
            return copy( ( TreeSet<V> ) collection );
        }
    }


    @Universal
    public static <V> Set<V> copy( Set<V> collection ) {
        if ( collection instanceof NavigableSet ) {

            return copy( ( NavigableSet<V> ) collection );


        } else if ( collection instanceof CopyOnWriteArraySet ) {

            return copy( ( CopyOnWriteArraySet<V> ) collection );

        } else {

            return copy( ( LinkedHashSet<V> ) collection );
        }
    }




    public static <V> Set<V> deepCopy( Collection<V> collection ) {
        Set<V> newSet = new LinkedHashSet<>(collection.size());

        for (V v : collection) {
            newSet.add(BeanUtils.copy(v));
        }
        return newSet;
    }

    public static <V> Set<V> deepCopyToSet( Collection<V> src,  Set<V> dst) {

        for (V v : src) {
            dst.add( BeanUtils.copy( v ));
        }
        return  dst;
    }


    public static <V,T> List<T> deepCopy( Collection<V> src, Class<T> dest  ) {
        List<T> list = new ArrayList<>(src.size());

        for (V v : src) {
            list.add( BeanUtils.createFromSrc( v, dest ));
        }
        return list;
    }

    @Universal
    public static <V> Set<V> deepCopy( Set<V> set ) {
        if ( set instanceof LinkedHashSet ) {
            return deepCopyToSet(set, new LinkedHashSet<V>());
        } else if ( set instanceof CopyOnWriteArraySet ) {
            return deepCopyToSet(set, new CopyOnWriteArraySet<V>());
        } else if ( set instanceof HashSet ) {
            return deepCopyToSet(set, new HashSet<V>());
        } else {
            return deepCopy( (Collection)set);
        }
    }

    //end universal

    public static List<Map<String, Object>> toListOfMaps( Set<?> set ) {
        return MapObjectConversion.toListOfMaps(set);

    }


    public static <T> Set<T> setFromProperty( Class<T> propertyType, String propertyPath, Collection<?> list ) {
        Set<T> newSet = new LinkedHashSet<>( list.size() );

        for ( Object item : list ) {
            T newItem = ( T ) BeanUtils.idx( item, propertyPath );
            newSet.add(newItem);
        }

        return newSet;

    }


    public static <T> Set<T> setFromProperty( Class<T> propertyType, String propertyPath, Iterable<?> list ) {
        Set<T> newSet = new LinkedHashSet<>(  );

        for ( Object item : list ) {
            T newItem = ( T ) BeanUtils.idx( item, propertyPath );
            newSet.add(newItem);
        }

        return newSet;

    }


    public static String toPrettyJson(Set set) {
        CharBuf buf = CharBuf.createCharBuf();
        return buf.prettyPrintCollection(set, false, 0).toString();
    }


}
