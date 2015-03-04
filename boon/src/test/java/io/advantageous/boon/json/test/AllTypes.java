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

package io.advantageous.boon.json.test;

import io.advantageous.boon.json.annotations.JsonIgnore;
import io.advantageous.boon.json.annotations.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@JsonIgnoreProperties ("ignoreMe2")
public class AllTypes {


    public Animal pet2;

    public Pet pet;


    public FooBasket getFooBasket () {
        return fooBasket;
    }

    public void setFooBasket ( FooBasket fooBasket ) {
        this.fooBasket = fooBasket;
    }

    FooBasket fooBasket = new FooBasket ();

    public String ignoreMe3;

    public String ignoreMe2;

    int myInt;
    boolean myBoolean;
    short myShort;
    long myLong;
    String string;
    String string2;
    BigDecimal bigDecimal;
    BigInteger bigInteger;
    Date date;

    float myFloat;
    double myDouble;
    byte myByte;

    FooEnum foo;
    FooEnum bar;

    @JsonIgnore
    public String ignoreMe;



    public long someTimeStamp = new Date (  ).getTime ();



    AllTypes allType;

    List<AllTypes> allTypeList = new ArrayList<> (  );


    Set<AllTypes> allTypesSet = new HashSet<> (  );


    public Set<AllTypes> getAllTypesSet() {
        return allTypesSet;
    }

    public void setAllTypesSet( Set<AllTypes> allTypesSet ) {
        this.allTypesSet = allTypesSet;
    }

    public String getString2 () {
        return string2;
    }

    public void setString2 ( String string2 ) {
        this.string2 = string2;
    }

    public List<AllTypes> getAllTypeList() {
        return allTypeList;
    }

    public void setAllTypeList( List<AllTypes> allTypeList ) {
        this.allTypeList = allTypeList;
    }

    public AllTypes getAllType () {
        return allType;
    }

    public void setAllType ( AllTypes allType ) {
        this.allType = allType;
    }

    public byte getMyByte () {
        return myByte;
    }

    public void setMyByte ( byte myByte ) {
        this.myByte = myByte;
    }

    public int getMyInt () {
        return myInt;
    }

    public void setMyInt ( int myInt ) {
        this.myInt = myInt;
    }

    public boolean isMyBoolean () {
        return myBoolean;
    }

    public void setMyBoolean ( boolean myBoolean ) {
        this.myBoolean = myBoolean;
    }

    public short getMyShort () {
        return myShort;
    }

    public void setMyShort ( short myShort ) {
        this.myShort = myShort;
    }

    public long getMyLong () {
        return myLong;
    }

    public void setMyLong ( long myLong ) {
        this.myLong = myLong;
    }

    public String getString () {
        return string;
    }

    public void setString ( String string ) {
        this.string = string;
    }


    public float getMyFloat () {
        return myFloat;
    }

    public void setMyFloat ( float myFloat ) {
        this.myFloat = myFloat;
    }

    public double getMyDouble () {
        return myDouble;
    }

    public void setMyDouble ( double myDouble ) {
        this.myDouble = myDouble;
    }


    public BigDecimal getBigDecimal () {
        return bigDecimal;
    }

    public void setBigDecimal ( BigDecimal bigDecimal ) {
        this.bigDecimal = bigDecimal;
    }

    public BigInteger getBigInteger () {
        return bigInteger;
    }

    public void setBigInteger ( BigInteger bigInteger ) {
        this.bigInteger = bigInteger;
    }


    public Date getDate () {
        return date;
    }

    public void setDate ( Date date ) {
        this.date = date;
    }


    public FooEnum getFoo () {
        return foo;
    }

    public void setFoo ( FooEnum foo ) {
        this.foo = foo;
    }

    public FooEnum getBar () {
        return bar;
    }

    public void setBar ( FooEnum bar ) {
        this.bar = bar;
    }


    @Override
    public boolean equals ( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof AllTypes ) ) return false;

        AllTypes allTypes1 = ( AllTypes ) o;

        if ( myBoolean != allTypes1.myBoolean ) return false;
        if ( myByte != allTypes1.myByte ) return false;
        if ( Double.compare ( allTypes1.myDouble, myDouble ) != 0 ) return false;
        if ( Float.compare ( allTypes1.myFloat, myFloat ) != 0 ) return false;
        if ( myInt != allTypes1.myInt ) return false;
        if ( myLong != allTypes1.myLong ) return false;
        if ( myShort != allTypes1.myShort ) return false;
        if ( bigDecimal != null ? !bigDecimal.equals ( allTypes1.bigDecimal ) : allTypes1.bigDecimal != null )
            return false;
        if ( bigInteger != null ? !bigInteger.equals ( allTypes1.bigInteger ) : allTypes1.bigInteger != null )
            return false;
        if ( string != null ? !string.equals ( allTypes1.string ) : allTypes1.string != null ) return false;
        if ( string2 != null ? !string2.equals ( allTypes1.string2 ) : allTypes1.string2 != null ) return false;

        if ( allTypeList == null && allTypes1.allTypeList.size () == 0) {
            return true;
        } else {

            if (allTypeList.size() == allTypes1.allTypeList.size()) {

                for (int index = 0; index < allTypeList.size(); index++) {
                    AllTypes theirs = allTypes1.allTypeList.get(index);
                    AllTypes ours = allTypeList.get( index );
                    if (!ours.equals( theirs ))  {
                        return false;
                    }
                }
            } else {
                return false;
            }

        }

        if ( date != null  && allTypes1.date!=null) {

            long delta = Math.abs ( date.getTime () - allTypes1.date.getTime ());

            if ( delta < 1000) {
                return true;
            } else {
                return false;
            }

        }

       if ( allType != null ? !allType.equals ( allTypes1.allType ) : allTypes1.allType != null ) return false;
        if ( bar != allTypes1.bar ) return false;
        if ( foo != allTypes1.foo ) return false;


        return true;
    }

    @Override
    public int hashCode () {
        int result;
        long temp;
        result = myInt;
        result = 31 * result + ( myBoolean ? 1 : 0 );
        result = 31 * result + ( int ) myShort;
        result = 31 * result + ( int ) ( myLong ^ ( myLong >>> 32 ) );
        result = 31 * result + ( string != null ? string.hashCode () : 0 );
        result = 31 * result + ( string2 != null ? string2.hashCode () : 0 );
        result = 31 * result + ( bigDecimal != null ? bigDecimal.hashCode () : 0 );
        result = 31 * result + ( bigInteger != null ? bigInteger.hashCode () : 0 );
        result = 31 * result + ( date != null ? date.hashCode () : 0 );
        result = 31 * result + ( myFloat != +0.0f ? Float.floatToIntBits ( myFloat ) : 0 );
        temp = Double.doubleToLongBits ( myDouble );
        result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        result = 31 * result + ( int ) myByte;
        result = 31 * result + ( foo != null ? foo.hashCode () : 0 );
        result = 31 * result + ( bar != null ? bar.hashCode () : 0 );
        result = 31 * result + ( allType != null ? allType.hashCode () : 0 );
        result = 31 * result + ( allTypeList != null ? allTypeList.hashCode () : 0 );
        return result;
    }

    @Override
    public String toString () {
        return "AllTypes{" +
                "myInt=" + myInt +
                ", myBoolean=" + myBoolean +
                ", myShort=" + myShort +
                ", myLong=" + myLong +
                ", string='" + string + '\'' +
                ", string2='" + string2 + '\'' +
                ", bigDecimal=" + bigDecimal +
                ", bigInteger=" + bigInteger +
                ", date=" + date +
                ", myFloat=" + myFloat +
                ", myDouble=" + myDouble +
                ", myByte=" + myByte +
                ", foo=" + foo +
                ", bar=" + bar +
                ", allType=" + allType +
                ", allTypeList=" + allTypeList +
                '}';
    }
}
