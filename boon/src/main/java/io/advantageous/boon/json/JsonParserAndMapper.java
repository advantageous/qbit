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

package io.advantageous.boon.json;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface JsonParserAndMapper extends JsonParser {


    Map<String, Object> parseMap( String value );
    Map<String, Object> parseMap( char [] value );
    Map<String, Object> parseMap( byte[] value );
    Map<String, Object> parseMap( byte[] value, Charset charset );
    Map<String, Object> parseMap( InputStream value, Charset charset );
    Map<String, Object> parseMap( CharSequence value );
    Map<String, Object> parseMap( InputStream value );
    Map<String, Object> parseMap( Reader value );
    Map<String, Object> parseMapFromFile(  String file );

    <T> List<T>  parseList(  Class<T> componentType, String jsonString );
    <T> List<T>  parseList(  Class<T> componentType, InputStream input );
    <T> List<T>  parseList(  Class<T> componentType, Reader reader );
    <T> List<T>  parseList(  Class<T> componentType, InputStream input, Charset charset );
    <T> List<T>  parseList(  Class<T> componentType, byte[] jsonBytes );
    <T> List<T>  parseList(  Class<T> componentType, byte[] jsonBytes, Charset charset );
    <T> List<T>  parseList(  Class<T> componentType, char[] chars );
    <T> List<T>  parseList(  Class<T> componentType, CharSequence jsonSeq );
    <T> List<T>  parseListFromFile(  Class<T> componentType, String fileName );

    <T> T parse( Class<T> type, String jsonString );
    <T> T parse( Class<T> type, byte[] bytes );
    <T> T parse( Class<T> type, byte[] bytes, Charset charset );
    <T> T parse( Class<T> type, CharSequence charSequence );
    <T> T parse( Class<T> type, char[] chars );
    <T> T parse( Class<T> type, Reader reader );
    <T> T parse( Class<T> type, InputStream input );
    <T> T parse( Class<T> type, InputStream input, Charset charset );
    <T> T parseDirect( Class<T> type, byte[] value );
    <T> T parseAsStream( Class<T> type, byte[] value );
    <T> T parseFile( Class<T> type,  String fileName);



    int  parseInt(  String jsonString );
    int  parseInt(  InputStream input );
    int  parseInt(  InputStream input, Charset charset );
    int  parseInt(  byte[] jsonBytes );
    int  parseInt(  byte[] jsonBytes, Charset charset );
    int  parseInt(  char[] chars );
    int  parseInt(  CharSequence jsonSeq );
    int  parseIntFromFile(  String fileName );

    long  parseLong(  String jsonString );
    long  parseLong(  InputStream input );
    long  parseLong(  InputStream input, Charset charset );
    long  parseLong(  byte[] jsonBytes );
    long  parseLong(  byte[] jsonBytes, Charset charset );
    long  parseLong(  char[] chars );
    long  parseLong(  CharSequence jsonSeq );
    long  parseLongFromFile(  String fileName );



    String  parseString(  String value );
    String  parseString(  InputStream value );
    String  parseString(  InputStream value, Charset charset );
    String  parseString(  byte[] value );
    String  parseString(  byte[] value, Charset charset );
    String  parseString(  char[] value );
    String  parseString(  CharSequence value );
    String  parseStringFromFile(  String value );

    double  parseDouble(  String value );
    double  parseDouble(  InputStream value );
    double  parseDouble(  byte[] value );
    double  parseDouble(  char[] value );
    double  parseDouble(  CharSequence value );
    double  parseDouble(  byte[] value, Charset charset );
    double  parseDouble(  InputStream value, Charset charset );
    double  parseDoubleFromFile(  String fileName );

    float  parseFloat(  String value );
    float  parseFloat(  InputStream value );
    float  parseFloat(  byte[] value );
    float  parseFloat(  char[] value );
    float  parseFloat(  CharSequence value );
    float  parseFloat(  byte[] value, Charset charset );
    float  parseFloat(  InputStream value, Charset charset );
    float  parseFloatFromFile(  String fileName );


    BigDecimal  parseBigDecimal(  String value );
    BigDecimal  parseBigDecimal(  InputStream value );
    BigDecimal  parseBigDecimal(  byte[] value );
    BigDecimal  parseBigDecimal(  char[] value );
    BigDecimal  parseBigDecimal(  CharSequence value );
    BigDecimal  parseBigDecimal(  byte[] value, Charset charset );
    BigDecimal  parseBigDecimal(  InputStream value, Charset charset );
    BigDecimal  parseBigDecimalFromFile(  String fileName );


    BigInteger  parseBigInteger(  String value );
    BigInteger  parseBigInteger(  InputStream value );
    BigInteger  parseBigInteger(  byte[] value );
    BigInteger  parseBigInteger(  char[] value );
    BigInteger  parseBigInteger(  CharSequence value );
    BigInteger  parseBigInteger(  byte[] value, Charset charset );
    BigInteger  parseBigInteger(  InputStream value, Charset charset );
    BigInteger  parseBigIntegerFile(  String fileName );

    Date  parseDate(  String jsonString );
    Date  parseDate(  InputStream input );
    Date  parseDate(  InputStream input, Charset charset );
    Date  parseDate(  byte[] jsonBytes );
    Date  parseDate(  byte[] jsonBytes, Charset charset );
    Date  parseDate(  char[] chars );
    Date  parseDate(  CharSequence jsonSeq );
    Date  parseDateFromFile(  String fileName );



    short  parseShort (  String jsonString );
    byte   parseByte  (  String jsonString );
    char   parseChar  (  String jsonString );
    <T extends Enum> T  parseEnum (  Class<T> type, String jsonString );

    public char     [] parseCharArray   ( String jsonString );
    public byte     [] parseByteArray   ( String jsonString );
    public short    [] parseShortArray  ( String jsonString );
    public int      [] parseIntArray    ( String jsonString );
    public float    [] parseFloatArray  ( String jsonString );
    public double   [] parseDoubleArray ( String jsonString );
    public long     [] parseLongArray   ( String jsonString );


    Object parse(  String jsonString );
    Object parse(  byte[] bytes );
    Object parse(  byte[] bytes, Charset charset );
    Object parse(  CharSequence charSequence );
    Object parse(  char[] chars );
    Object parse(  Reader reader );
    Object parse(  InputStream input );
    Object parse(  InputStream input, Charset charset );
    Object parseDirect(  byte[] value );
    Object parseAsStream(  byte[] value );
    Object parseFile(  String fileName);


    void close();

}
