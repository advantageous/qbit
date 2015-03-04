package io.advantageous.boon;


import static io.advantageous.boon.Exceptions.requireNonNull;

/**
* Created by Richard on 9/9/14.
*/
public class Pair<K, V> implements Entry<K, V> {


    public static <K, V> Entry<K, V> entry( final K k, final V v ) {
        return new Pair<>( k, v );
    }


    public static <K, V> Pair<K, V> pair( final K k, final V v ) {
        return new Pair<>( k, v );
    }

    public static <K, V> Entry<K, V> entry( Entry<K, V> entry ) {
        return new Pair<>( entry );
    }

    private K k;
    private V v;

    public Pair() {

    }

    public Pair(Pair<K, V> impl) {
        requireNonNull( impl );
        requireNonNull( impl.k );

        this.k = impl.k;
        this.v = impl.v;
    }

    public Pair(Entry<K, V> entry) {
        requireNonNull( entry );
        requireNonNull( entry.key() );

        this.k = entry.key();
        this.v = entry.value();
    }

    public Pair(K k, V v) {
        Exceptions.requireNonNull( k );

        this.k = k;
        this.v = v;
    }

    @Override
    public K key() {
        return k;
    }


    public K getFirst() {
        return k;
    }
    public V getSecond() {
        return v;
    }

    @Override
    public V value() {
        return v;
    }


    @Override
    public K getKey() {
        return k;
    }

    @Override
    public V getValue() {
        return v;
    }

    @Override
    public V setValue(V value) {
        V old = this.v;
        this.v = value;
        return old;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Pair entry = (Pair) o;
        return this.equals( entry );
    }

    @Override
    public boolean equals( Entry entry ) {

        if ( k != null ? !k.equals( entry.key() ) : entry.key() != null ) return false;
        return !( v != null ? !v.equals( entry.value() ) : entry.value() != null );

    }

    @Override
    public int hashCode() {
        int result = k != null ? k.hashCode() : 0;
        result = 31 * result + ( v != null ? v.hashCode() : 0 );
        return result;
    }

    @Override
    public int compareTo( Entry entry ) {
        requireNonNull( entry );
        return this.key().toString().compareTo( entry.key().toString() );
    }

    @Override
    public String toString() {
        return "{" +
                "\"k\":" + k +
                ", \"v\":" + v +
                '}';
    }

    public void setFirst(K first) {
        this.k = first;
    }

    public void setSecond(V v) {
        this.v = v;
    }
}
