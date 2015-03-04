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

package io.advantageous.boon.core;

import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.reflection.FastStringUtils;
import io.advantageous.boon.json.JsonException;
import io.advantageous.boon.primitive.CharScanner;
import io.advantageous.boon.primitive.CharBuf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Dates {

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone( "UTC" );
    private static volatile long lastNow;
    private static long MILLI_SECOND = 1;
    private static long SECOND = MILLI_SECOND * 1000;
    private static long MINUTE = 60 * SECOND;
    private static long HOUR = 60 * MINUTE;
    private static long DAY = 24 * HOUR;
    private static long WEEK = 7 * DAY;
    private static long MONTH = ( long ) ( 30.4167 * DAY );
    private static long YEAR = ( long ) ( 365.2425 * DAY );


    public static long utcNow() {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( now );
        calendar.setTimeZone( UTC_TIME_ZONE );
        long utcNow = calendar.getTime().getTime();
        lastNow = now;
        return utcNow;
    }

    public static long now() {
        return System.currentTimeMillis();
    }


    public static long timeZoneNow(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        return timeZoneNow(timeZone);
    }


    public static long timeZoneNow(TimeZone timeZone, Calendar calendar) {
        long now = System.currentTimeMillis();
        calendar.setTimeInMillis( now );
        calendar.setTimeZone( timeZone );
        long timeZoneNow = calendar.getTime().getTime();
        return timeZoneNow;
    }



    public static Calendar utcCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone( UTC_TIME_ZONE );
        return  calendar;
    }

    public static int durationInHours(long to, long from) {
        long duration = Math.abs ( to - from );

        return  (int) (duration / HOUR);

    }


    public static int durationInMinutes(long to, long from) {
        long duration = Math.abs ( to - from );

        return  (int) (duration / MINUTE);

    }


    public static long durationInSeconds(long to, long from) {
        long duration = Math.abs ( to - from );

        return  (int) (duration / SECOND);

    }


    public static long durationInMilis(long to, long from) {
        long duration = Math.abs ( to - from );

        return  (int) (duration / MILLI_SECOND);

    }


    public static long utcNowFast(Calendar utcCalendar) {
        long now = Sys.time();
        long utcNow = utcCalendar.getTime().getTime();
        lastNow = now;
        return utcNow;
    }

    public static long utc( long time ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( time );
        calendar.setTimeZone( UTC_TIME_ZONE );
        long utcNow = calendar.getTime().getTime();
        lastNow = time;
        return utcNow;
    }



    public static long utcFast( long time, Calendar utcCalendar ) {
        long utcNow = utcCalendar.getTime().getTime();
        lastNow = time;
        return utcNow;
    }

    /**
     * For testing only, avoids potential timing issue.
     */
    static long lastNow() {
        return lastNow;
    }


    public static long fromUtcTimeToTimeZone( long utcTime, TimeZone timeZone ) {

        Calendar calendar = Calendar.getInstance( UTC_TIME_ZONE );
        calendar.setTimeInMillis( utcTime );
        calendar.setTimeZone( timeZone );
        return calendar.getTime().getTime();
    }


    public static boolean before( long isThis, long beforeThis ) {
        return isThis < beforeThis;
    }


    public static boolean after( long isThis, long afterThis ) {
        return isThis > afterThis;
    }

    public static long hourDuration (int count ) {
        return count * HOUR;
    }


    public static long minuteDuration (int count ) {
        return count * MINUTE;
    }


    public static long secondDuration (int count ) {
        return count * SECOND;
    }


    public static long dayDuration (int count ) {
        return count * DAY;
    }

    public static long secondsFrom( long time, int seconds ) {
        return time + ( seconds * SECOND );
    }

    public static long minutesFrom( long time, int minutes ) {
        return time + ( minutes * MINUTE );
    }

    public static long hoursFrom( long time, int hours ) {
        return time + ( hours * HOUR );
    }

    public static long daysFrom( long time, int days ) {
        return time + ( days * DAY );
    }

    public static long weeksFrom( long time, int weeks ) {
        return time + ( weeks * WEEK );
    }

    public static long monthsFrom( long time, int months ) {
        return time + ( months * MONTH );
    }

    public static long yearsFrom( long time, int years ) {
        return time + ( years * YEAR );
    }

    public static long utcDate( int year, int month, int day ) {
        Calendar calendar = Calendar.getInstance();

        /* Set to midnight. */
        midnight( calendar );

        /* This might change the date, but when you convert it
        back to the clocktime timezone, it will be correct.
         */
        calendar.setTimeZone( UTC_TIME_ZONE );


        return internalDate( year, month, day, calendar );
    }

    public static long utcDate( int year, int month, int day,
                                int hour, int minute ) {
        Calendar calendar = Calendar.getInstance();
        midnight( calendar );

        /* This might change the date, but when you convert it
        back to the clocktime timezone, it will be correct.
         */
        calendar.setTimeZone( UTC_TIME_ZONE );

        return internalDateLong( year, month, day, hour, minute, calendar );
    }

    private static long internalDateLong( int year, int month, int day, int hour, int minute, Calendar calendar ) {

        return internalDate( year, month, day, hour, minute, calendar ).getTime();

    }


    private static Date internalDate( int year, int month, int day, int hour, int minute, Calendar calendar ) {
        calendar.set( Calendar.YEAR, year );
        calendar.set( Calendar.MONTH, month );
        calendar.set( Calendar.DAY_OF_MONTH, day );
        calendar.set( Calendar.HOUR_OF_DAY, hour );
        calendar.set( Calendar.MINUTE, minute );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MILLISECOND, 0 );


        return calendar.getTime();
    }


    private static Date internalDate( TimeZone tz, int year, int month, int day, int hour, int minute, int second ) {

        Calendar calendar = Calendar.getInstance();

        calendar.set( Calendar.YEAR, year );
        calendar.set( Calendar.MONTH, month - 1 );
        calendar.set( Calendar.DAY_OF_MONTH, day );
        calendar.set( Calendar.HOUR_OF_DAY, hour );
        calendar.set( Calendar.MINUTE, minute );
        calendar.set( Calendar.SECOND, second );
        calendar.set( Calendar.MILLISECOND, 0 );

        calendar.setTimeZone( tz );

        return calendar.getTime();
    }


    private static Date internalDate( TimeZone tz, int year, int month, int day, int hour,
                                      int minute, int second, int miliseconds ) {

        Calendar calendar = Calendar.getInstance();

        calendar.set( Calendar.YEAR, year );
        calendar.set( Calendar.MONTH, month - 1 );
        calendar.set( Calendar.DAY_OF_MONTH, day );
        calendar.set( Calendar.HOUR_OF_DAY, hour );
        calendar.set( Calendar.MINUTE, minute );
        calendar.set( Calendar.SECOND, second );
        calendar.set( Calendar.MILLISECOND, miliseconds );

        calendar.setTimeZone( tz );

        return calendar.getTime();
    }

    public static long wallTimeDate( int year, int month, int day ) {
        Calendar calendar = Calendar.getInstance();

        /* Set to midnight. */
        midnight( calendar );


        return internalDate( year, month, day, calendar );
    }


    public static long date( int year, int month, int day ) {
        return utcDate( year, month, day );
    }

    public static long date( int year, int month, int day,
                             int hour, int minute ) {
        return utcDate( year, month, day, hour, minute );

    }


    public static long date( TimeZone tz, int year, int month, int day ) {
        Calendar calendar = Calendar.getInstance();

        /* Set to midnight. */
        midnight( calendar );

        calendar.setTimeZone( tz );

        return internalDate( year, month, day, calendar );
    }

    private static long internalDate( int year, int month, int day, Calendar calendar ) {
        calendar.set( Calendar.YEAR, year );
        calendar.set( Calendar.MONTH, month );
        calendar.set( Calendar.DAY_OF_MONTH, day );

        calendar.set( Calendar.HOUR_OF_DAY, 0 );

        calendar.set( Calendar.MINUTE, 0 );

        calendar.set( Calendar.SECOND, 0 );

        calendar.set( Calendar.MILLISECOND, 0 );


        return calendar.getTime().getTime();
    }

    public static long wallTimeDate( int year, int month, int day,
                                     int hour, int minute ) {
        Calendar calendar = Calendar.getInstance();
        midnight( calendar );


        return internalDateLong( year, month, day, hour, minute, calendar );
    }


    public static Date toDate( TimeZone tz, int year, int month, int day,
                               int hour, int minute, int second ) {
        return internalDate( tz, year, month, day, hour, minute, second );
    }


    public static Date toDate( TimeZone tz, int year, int month, int day,
                               int hour, int minute, int second, int miliseconds ) {
        return internalDate( tz, year, month, day, hour, minute, second, miliseconds );
    }

    public static Date toDate( int year, int month, int day,
                               int hour, int minute, int second, int miliseconds ) {
        return internalDate( TimeZone.getDefault(), year, month, day, hour, minute, second, miliseconds );
    }

    public static long date( TimeZone tz, int year, int month, int day,
                             int hour, int minute ) {
        Calendar calendar = Calendar.getInstance();
        midnight( calendar );
        calendar.setTimeZone( tz );

        return internalDateLong( year, month, day, hour, minute, calendar );
    }

    private static void midnight( Calendar calendar ) {
        /* Set to midnight. */
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MILLISECOND, 0 );
    }

    /**
     * Useful for generating string versions of timestamps
     *
     * @return euro style format.
     */
    public static String euroUTCSystemDateNowString() {
        long now = System.currentTimeMillis();
        return euroUTCSystemDateString( now );
    }


    /**
     * Useful for generated file names and generated work directories.
     *
     * @param timestamp the timestamp
     * @return euro style format.
     */
    public static String euroUTCSystemDateString( long timestamp ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( timestamp );
        calendar.setTimeZone( UTC_TIME_ZONE );
        int day = calendar.get( Calendar.DAY_OF_MONTH );
        int month = calendar.get( Calendar.MONTH );
        int year = calendar.get( Calendar.YEAR );
        int hour = calendar.get( Calendar.HOUR_OF_DAY );
        int minute = calendar.get( Calendar.MINUTE );
        int second = calendar.get( Calendar.SECOND );

        CharBuf buf = CharBuf.create( 16 );
        buf.add( Str.zfill(day, 2) ).add( '_' );
        buf.add( Str.zfill( month, 2 ) ).add( '_' );
        buf.add( year ).add( '_' );
        buf.add( Str.zfill( hour, 2 ) ).add( '_' );
        buf.add( Str.zfill( minute, 2 ) ).add( '_' );
        buf.add( Str.zfill( second, 2 ) ).add( "_utc_euro" );

        return buf.toString();
    }


    public static void main( String... args ) {

        Sys.println( euroUTCSystemDateNowString() );

    }


    public static Date year( int year ) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(GMT);
        c.set( 1970, Calendar.JANUARY, 2, 0, 0, 0 );
        c.set( Calendar.YEAR, year );
        c.set( Calendar.MILLISECOND, 0 );
        return c.getTime();
    }

    public static Date getUSDate( int month, int day, int year ) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(GMT);
        c.set( year, month - 1, day + 1, 0, 0, 0 );
        c.set( Calendar.MILLISECOND, 0 );
        return c.getTime();
    }


    public static Date getUSDate( int month, int day, int year, int hour, int minute, int second ) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(GMT);
        c.set( year, month - 1, day + 1, hour, minute, second );
        c.set( Calendar.MILLISECOND, 0 );
        return c.getTime();
    }

    public static Date getEuroDate( int day, int month, int year ) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(GMT);
        c.set( year, month - 1, day + 1, 0, 0, 0 );
        c.set( Calendar.MILLISECOND, 0 );
        return c.getTime();
    }

    public static Date getEuroDate( int day, int month, int year, int hour, int minute, int second ) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(GMT);
        c.set( year, month - 1, day + 1, hour, minute, second );
        c.set( Calendar.MILLISECOND, 0 );
        return c.getTime();
    }


    public static Date fromISO8601_( String string ) {

        try {

            return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssXXX" ).parse( string );
        } catch ( ParseException e ) {
            return Exceptions.handle(Date.class, "Not a valid ISO8601", e);
        }


    }

    public static Date fromISO8601Jackson_(String string) {


        if (string.length() == 29 && Str.idx(string, -3) == ':') {

            try {

                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(string);
            } catch (ParseException e) {
                return Exceptions.handle(Date.class, "Not a valid ISO8601 \"Jackson\" date", e);
            }


        } else {

            try {

                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(string);
            } catch (ParseException e) {
                return Exceptions.handle(Date.class, "Not a valid ISO8601 \"Jackson\" date", e);
            }

        }


    }

    public static Date fromJsonDate_( String string ) {

        try {

            return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" ).parse( string );
        } catch ( ParseException e ) {
            return Exceptions.handle( Date.class, "Not a valid JSON date", e );
        }


    }


    public static Date fromJsonDate( String string ) {

        return fromJsonDate( FastStringUtils.toCharArray(string), 0, string.length() );

    }

    public static Date fromISO8601( String string ) {

        return fromISO8601( FastStringUtils.toCharArray( string ), 0, string.length() );

    }

    public static Date fromISO8601Jackson(String string) {

        return fromISO8601Jackson(FastStringUtils.toCharArray(string), 0, string.length());

    }

    public static Date fromISO8601DateLoose( String string ) {
        return fromISO8601DateLoose( FastStringUtils.toCharArray( string ), 0, string.length() );

    }

    public static Date fromISO8601DateLoose( char [] chars ) {
        return fromISO8601DateLoose( chars, 0, chars.length );

    }


    final static int SHORT_ISO_8601_TIME_LENGTH = "1994-11-05T08:15:30Z".length();
    final static int LONG_ISO_8601_TIME_LENGTH = "1994-11-05T08:15:30-05:00".length();
    final static int LONG_ISO_8601_JACKSON_TIME_LENGTH = "1994-11-05T08:11:22.123-0500".length();
    public final static int JSON_TIME_LENGTH = "2013-12-14T01:55:33.412Z".length();


    /**
     *
     * @param date the timestamp
     * @return json style format.
     */
    public static String jsonDate( Date date ) {
        CharBuf buf = CharBuf.create( JSON_TIME_LENGTH );
        jsonDateChars ( date, buf );
        return buf.toString();
    }


    private final static boolean  isGMT;
    static {
        if (TimeZone.getDefault () == GMT) {
            isGMT = true;
        } else {
            isGMT = false;
        }

    }

    public static void jsonDateChars( Date date, CharBuf buf ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone (GMT);
        jsonDateChars( calendar, date, buf );
    }


    public static void jsonDateChars( long milis, CharBuf buf ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone (GMT);
        jsonDateChars( calendar, milis, buf );
    }

    public static void jsonDateChars( Calendar calendar, Date date, CharBuf buf ) {
        jsonDateChars(calendar, date.getTime(), buf);
    }

    public static void jsonDateChars( Calendar calendar, long milis, CharBuf buf ) {
        if (isGMT) {
            /* For the Unix admins of the world who avoid it all and just GMT it. */
            fastJsonDateChars ( new Date(milis), buf );
            return;
        }

        calendar.setTimeInMillis( milis );


        int day = calendar.get( Calendar.DAY_OF_MONTH );
        int month = calendar.get( Calendar.MONTH ) +1;
        int year = calendar.get( Calendar.YEAR );
        int hour = calendar.get( Calendar.HOUR_OF_DAY );
        int minute = calendar.get( Calendar.MINUTE );
        int second = calendar.get( Calendar.SECOND );
        int mili = calendar.get( Calendar.MILLISECOND );

        buf.add( '"' );
        buf.add( year ).add( '-' );
        buf.add( Str.zfill( month, 2 ) ).add( '-' );
        buf.add( Str.zfill ( day, 2 ) ).add('T');

        buf.add( Str.zfill( hour, 2 ) ).add( ':' );
        buf.add( Str.zfill( minute, 2 ) ).add( ':' );
        buf.add( Str.zfill( second, 2 ) ).add( "." );
        buf.add( Str.zfill( mili, 3 ) ).add( "Z" );

        buf.add( '"' );

    }

    @SuppressWarnings("deprecation")
    public static void fastJsonDateChars( Date date, CharBuf buf ) {

        int day = date.getDate ();
        int month = date.getMonth () +1;
        int year = date.getYear () + 1900;
        int hour = date.getHours ();
        int minute = date.getMinutes ();
        int second = date.getSeconds ();
        int offset = date.getTimezoneOffset ();
        int mili = 1;

        buf.add( '"' );
        buf.add( year ).add( '-' );
        buf.add( Str.zfill( month, 2 ) ).add( '-' );
        buf.add( Str.zfill ( day, 2 ) ).add('T');

        buf.add( Str.zfill( hour, 2 ) ).add( ':' );
        buf.add( Str.zfill( minute, 2 ) ).add( ':' );
        buf.add( Str.zfill( second, 2 ) ).add( "." );
        buf.add( Str.zfill( mili, 3 ) ).add( "Z" );

        buf.add( '"' );

    }

    public static Date fromISO8601DateLoose( char[] buffer, int startIndex, int endIndex ) {

        if ( Dates.isISO8601QuickCheck( buffer, startIndex, endIndex ) ) {

            if ( Dates.isJsonDate( buffer, startIndex, endIndex ) ) {
                return Dates.fromJsonDate( buffer, startIndex, endIndex );

            } else if ( Dates.isISO8601( buffer, startIndex, endIndex ) ) {
                return Dates.fromISO8601( buffer, startIndex, endIndex );
            } else {
                try {
                    return looseParse( buffer, startIndex, endIndex );
                } catch ( Exception ex ) {
                    throw new JsonException( "unable to do a loose parse", ex );
                }
            }
        } else {

            try {
                return looseParse( buffer, startIndex, endIndex );
            } catch ( Exception ex ) {
                throw new JsonException( "unable to do a loose parse", ex );
            }
        }


    }

    private static Date looseParse( char[] buffer, int startIndex, int endIndex ) {
        final char[][] parts = CharScanner.splitByCharsNoneEmpty(buffer, startIndex, endIndex, '-', ':', 'T', '.');
        int year = 0;
        int month = 0;
        int day = 0;

        int hour = 0;
        int minutes = 0;
        int seconds = 0;

        int mili = 0;

        if ( parts.length >= 3 ) {
            year = CharScanner.parseInt( parts[ 0 ] );
            month = CharScanner.parseInt( parts[ 1 ] );
            day = CharScanner.parseInt( parts[ 2 ] );
        }

        if ( parts.length >= 6 ) {
            hour = CharScanner.parseInt( parts[ 3 ] );
            minutes = CharScanner.parseInt( parts[ 4 ] );
            seconds = CharScanner.parseInt( parts[ 5 ] );
        }

        if ( parts.length >= 7 ) {
            mili = CharScanner.parseInt( parts[ 6 ] );
        }


        return toDate( year, month, day, hour, minutes, seconds, mili );
    }

    public static Date fromISO8601Jackson(char[] charArray, int from, int to) {

        try {
            if ( isISO8601Jackson(charArray, from, to) ) {
                int year = CharScanner.parseInt(charArray, from + 0, from + 4);
                int month = CharScanner.parseInt(charArray, from + 5, from + 7);
                int day = CharScanner.parseInt(charArray, from + 8, from + 10);
                int hour = CharScanner.parseInt(charArray, from + 11, from + 13);

                int minute = CharScanner.parseInt(charArray, from + 14, from + 16);

                int second = CharScanner.parseInt(charArray, from + 17, from + 19);
                int millisecond = CharScanner.parseInt(charArray, from + 20, from + 23);

                TimeZone tz = null;

                if ( charArray[ from + 19 ] == 'Z' ) {

                    tz = GMT;

                }

                else {
                    StringBuilder builder = new StringBuilder( 8 );
                    builder.append("GMT");

                    for (int index = from + 23; index < to; index++ ) {
                        if (charArray[index] == ':') {
                            continue;
                        }
                        builder.append( charArray[index] );
                    }
                    String tzStr = builder.toString();
                    tz = TimeZone.getTimeZone(tzStr);
                }
                return toDate( tz, year, month, day, hour, minute, second, millisecond );

            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }

    }

    public static Date fromISO8601( char[] charArray, int from, int to ) {

        try {
        int length = to - from;
        if ( isISO8601( charArray, from, to ) ) {
            int year = CharScanner.parseInt(charArray, from + 0, from + 4);
            int month = CharScanner.parseInt(charArray, from + 5, from + 7);
            int day = CharScanner.parseInt(charArray, from + 8, from + 10);
            int hour = CharScanner.parseInt(charArray, from + 11, from + 13);

            int minute = CharScanner.parseInt(charArray, from + 14, from + 16);

            int second = CharScanner.parseInt(charArray, from + 17, from + 19);
            TimeZone tz = null;

            if ( charArray[ from + 19 ] == 'Z' ) {

                tz = GMT;

            } else {

                StringBuilder builder = new StringBuilder( 9 );
                builder.append( "GMT" );
                builder.append( charArray, from + 19, 6 );
                String tzStr = builder.toString();
                tz = TimeZone.getTimeZone( tzStr );

            }
            return toDate( tz, year, month, day, hour, minute, second );

        } else {
            return null;
        }
        } catch (Exception ex) {
            return null;
        }

    }

    public static Date fromJsonDate( char[] charArray, int from, int to ) {
        try {
        if ( isJsonDate( charArray, from, to ) ) {
            int year = CharScanner.parseInt(charArray, from + 0, from + 4);
            int month = CharScanner.parseInt(charArray, from + 5, from + 7);
            int day = CharScanner.parseInt(charArray, from + 8, from + 10);
            int hour = CharScanner.parseInt(charArray, from + 11, from + 13);

            int minute = CharScanner.parseInt(charArray, from + 14, from + 16);

            int second = CharScanner.parseInt(charArray, from + 17, from + 19);

            int milliseconds = CharScanner.parseInt(charArray, from + 20, from + 23);

            TimeZone tz = GMT;


            return toDate( tz, year, month, day, hour, minute, second, milliseconds );

        } else {
            return null;
        }
        } catch (Exception ex) {
            return null;
        }

    }

    public static boolean isISO8601( String string ) {

        return isISO8601( FastStringUtils.toCharArray( string ) );
    }


    public static boolean isISO8601( char[] charArray ) {
        return isISO8601( charArray, 0, charArray.length );
    }

    public static boolean isISO8601( char[] charArray, int start, int to ) {
        boolean valid = true;
        final int length = to - start;

        if ( length == SHORT_ISO_8601_TIME_LENGTH ) {
            valid &= ( charArray[ start + 19 ] == 'Z' );

        } else if ( length == LONG_ISO_8601_TIME_LENGTH ) {
            valid &= ( charArray[ start + 19 ] == '-' || charArray[ start + 19 ] == '+' );
            valid &= ( charArray[ start + 22 ] == ':' );

        } else {
            return false;
        }

        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4
        // "1 9 9 4 - 1 1 - 0 5 T 0 8 : 1 5 : 3 0 - 0 5 : 0 0

        valid &= ( charArray[ start + 4 ] == '-' ) &&
                ( charArray[ start + 7 ] == '-' ) &&
                ( charArray[ start + 10 ] == 'T' ) &&
                ( charArray[ start + 13 ] == ':' ) &&
                ( charArray[ start + 16 ] == ':' );

        return valid;
    }

    public static boolean isISO8601Jackson(char[] charArray, int start, int to) {
        boolean valid = true;
        final int length = to - start;

        if ( length == SHORT_ISO_8601_TIME_LENGTH ) {
            valid &= ( charArray[ start + 19 ] == 'Z' );

        } else if ( length == LONG_ISO_8601_JACKSON_TIME_LENGTH || length == 29) {
            valid &= ( charArray[ start + 23 ] == '-' || charArray[ start + 23 ] == '+' );
        } else {
            return false;
        }

        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4
        // "1 9 9 4 - 1 1 - 0 5 T 0 8 : 1 5 : 3 0 - 0 5 : 0 0

        valid &= ( charArray[ start + 4 ] == '-' ) &&
                ( charArray[ start + 7 ] == '-' ) &&
                ( charArray[ start + 10 ] == 'T' ) &&
                ( charArray[ start + 13 ] == ':' ) &&
                ( charArray[ start + 16 ] == ':' );

        return valid;
    }

    public static boolean isISO8601QuickCheck( char[] charArray, int start, int to ) {
        final int length = to - start;

        try {

            if ( length == JSON_TIME_LENGTH || length == LONG_ISO_8601_TIME_LENGTH
                    || length == SHORT_ISO_8601_TIME_LENGTH || ( length >= 17 && ( charArray[ start + 16 ] == ':' ) )
                    ) {
                    return true;
            }

            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static boolean isISO8601QuickCheck( char[] charArray ) {
        final int length = charArray.length;

        if ( length == JSON_TIME_LENGTH || length == LONG_ISO_8601_TIME_LENGTH
                || length == SHORT_ISO_8601_TIME_LENGTH || ( length >= 16 && ( charArray[   16 ] == ':' ) )
                ) {

            if ( length >= 16 && ( charArray[  16 ] == ':' ) ) {
                return true;
            }
        }

        return false;

    }


    public static boolean isJsonDate( String str ) {

        return isJsonDate(FastStringUtils.toCharArray(str), 0, str.length());
    }

    public static boolean isJsonDate( char[] charArray, int start, int to ) {
        boolean valid = true;
        final int length = to - start;

        if ( length != JSON_TIME_LENGTH ) {
            return false;
        }

        valid &= ( charArray[ start + 19 ] == '.' );

        if ( !valid ) {
            return false;
        }


        valid &= ( charArray[ start + 4 ] == '-' ) &&
                ( charArray[ start + 7 ] == '-' ) &&
                ( charArray[ start + 10 ] == 'T' ) &&
                ( charArray[ start + 13 ] == ':' ) &&
                ( charArray[ start + 16 ] == ':' );

        return valid;
    }

}
