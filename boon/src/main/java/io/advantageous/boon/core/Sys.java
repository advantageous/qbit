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


import com.sun.management.UnixOperatingSystemMXBean;
import io.advantageous.boon.Exceptions;
import io.advantageous.boon.IO;
import io.advantageous.boon.Str;
import io.advantageous.boon.core.reflection.Annotations;
import io.advantageous.boon.core.timer.TimeKeeper;
import io.advantageous.boon.core.timer.TimeKeeperBasic;
import io.advantageous.boon.Lists;
import io.advantageous.boon.core.reflection.Reflection;
import io.advantageous.boon.json.JsonParserFactory;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class Sys {


    public static ConcurrentHashMap <Object, Object> systemProperties = new ConcurrentHashMap<>(System.getProperties());

    public static ConcurrentHashMap <String, String> env = new ConcurrentHashMap<>(System.getenv());


    private final static boolean isWindows = System.getProperty ( "os.name" ).contains ( "Windows" );
    private final static boolean inContainer;
    private final static boolean is1_7OorLater;
    private final static int buildNumber;
    private final static BigDecimal version;
    private final static boolean is1_7;
    private final static boolean is1_8;
    public final static Object DEFAULT_NULL_NOT_EMPTY = new Object();


    static {
        BigDecimal v = new BigDecimal ( "-1" );
        int b = -1;
        String sversion = System.getProperty ( "java.version" );
        if ( sversion.indexOf ( "_" ) != -1 ) {
            final String[] split = sversion.split ( "_" );
            try {

                String ver = split [0];
                if (ver.startsWith ( "1.8" )) {
                    v = new BigDecimal ("1.8" );
                }
                if (ver.startsWith ( "1.7" )) {
                    v = new BigDecimal ("1.7" );
                }

                if (ver.startsWith ( "1.6" )) {
                    v = new BigDecimal ("1.6" );
                }


                if (ver.startsWith ( "1.5" )) {
                    v = new BigDecimal ("1.5" );
                }


                if (ver.startsWith ( "1.9" )) {
                    v = new BigDecimal ("1.9" );
                }

                String build = split[ 1 ];
                if (build.endsWith("-ea")) {
                    build = build.substring(0, build.length() - 3);
                }
                b = Integer.parseInt ( build );
            } catch ( Exception ex ) {
                ex.printStackTrace ();
                System.err.println ( "Unable to determine build number or version" );
            }
        } else if ("1.8.0".equals(sversion) || "1.8.0-ea".equals(sversion)) {
            b = -1;
            v = new BigDecimal("1.8");
        } else {

            try {
                v = new BigDecimal(sversion);
                b = -1;
            }catch (Exception ex) {

                if (sversion.startsWith ( "1.7" )) {
                    v = new BigDecimal ("1.7" );
                } else if (sversion.startsWith ( "1.8" )) {
                    v = new BigDecimal ("1.8" );
                } else {
                    v = new BigDecimal ("-1.0" );
                }
            }
        }

        buildNumber = b;
        version = v;

        is1_7OorLater = version.compareTo ( new BigDecimal ( "1.7" )) >=0;
        is1_7 = version.compareTo ( new BigDecimal ( "1.7" ))==0;
        is1_8 = version.compareTo ( new BigDecimal ( "1.8" ))==0;
    }


    public static void println ( String message ) {
        System.out.println ( message );
    }

    public static void print ( String message ) {
        System.out.print ( message );
    }


    public static boolean isWindows () {
        return isWindows;
    }

    public static boolean is1_7OrLater () {
        return is1_7OorLater;
    }

    public static boolean is1_7() {
        return is1_7;
    }
    public static boolean is1_8() {
        return is1_8;
    }

    public static int buildNumber () {
        return buildNumber;
    }

    public static char windowsPathSeparator () {
        return '\\';
    }


    final static AtomicReference<TimeKeeper> timer = new AtomicReference<TimeKeeper> ( new TimeKeeperBasic() );

    public static TimeKeeper timer () {
        return timer.get ();
    }

    public static long time () {
        return timer.get ().time ();
    }


    static {
        boolean _inContainer;
        boolean forceInContainer = Boolean.parseBoolean ( System.getProperty ( "io.advantageous.boon.forceInContainer", "false" ) );
        boolean forceNoContainer = Boolean.parseBoolean ( System.getProperty ( "io.advantageous.boon.forceNoContainer", "false" ) );

        if ( forceNoContainer ) {
            _inContainer = false;
        } else if ( forceInContainer ) {
            _inContainer = true;
        } else {
            _inContainer = detectContainer ();
        }

        inContainer = _inContainer;
    }

    private static boolean detectContainer () {

        boolean _inContainer;

        try {
            Class.forName ( "javax.servlet.http.HttpServlet" );

            _inContainer = true;
        } catch ( ClassNotFoundException e ) {
            _inContainer = false;
        }
        if ( !_inContainer ) {
            try {
                Class.forName ( "javax.ejb.EJBContext" );

                _inContainer = true;
            } catch ( ClassNotFoundException e ) {
                _inContainer = false;
            }

        }

        return _inContainer;

    }


    public static boolean inContainer () {
        return inContainer;
    }


    /* Everything that has a cache you need to hold on to, should use this so they can
     * all be stuffed into application context of web-app or ear if you use Java EE. */
    public static Object contextToHold () {

        return Lists.list ( Reflection.contextToHold (),
                Annotations.contextToHold());
    }

    public static String sysPropMultipleKeys(String... keys) {
        for (String key : keys) {
            String value = _sysProp(key, DEFAULT_NULL_NOT_EMPTY);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static String sysProp(String key) {
        return _sysProp(key, null);
    }

    public static String sysPropDefaultNull(String key) {
        return _sysProp(key, DEFAULT_NULL_NOT_EMPTY);
    }


    /**
     * Checks for the key under system property.
     * Then checks it as an environment variable.
     * (Looks up in env using straight key and performing underBarCase on it.)
     * Then converts defaultValue into a string.
     * @param key key
     * @param defaultValue default value
     * @return prop
     */
    public static String sysProp(String key, Object defaultValue) {
        return _sysProp(key, defaultValue);
    }

    private static String _sysProp(String key, Object defaultValue) {
        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);

            if (property == null) {
                String newKey = Str.underBarCase(key);
                property = env.get(newKey);

                if (property == null && defaultValue != DEFAULT_NULL_NOT_EMPTY) {
                    property = Conversions.toString(defaultValue);
                }
            }
        }

        return property;
    }


    public static boolean sysPropBoolean(String key) {
        return sysProp(key, false);
    }


    public static boolean sysProp(String key, boolean defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toBoolean(property);

    }


    public static int sysPropInt(String key) {
        return sysProp(key, -1);
    }



    public static int sysProp(String key, int defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toInt(property);

    }

    public static File sysProp(String key, File defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return new File(property);

    }


    public static Path sysProp(String key, Path defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return  IO.path(property);
    }


    public static int sysPropLong(String key) {
        return sysProp(key, -1);
    }


    public static long sysProp(String key, long defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toLong(property);

    }


    public static short sysPropShort(String key) {
        return sysProp(key, (short) -1);
    }


    public static short sysProp(String key, short defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toShort(property);

    }


    public static byte sysPropByte(String key) {
        return sysProp(key, (byte) -1);
    }


    public static byte sysProp(String key, byte defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toByte(property);

    }


    public static BigDecimal sysPropBigDecimal(String key) {
        return sysPropBigDecima(key, (BigDecimal) null);
    }


    public static BigDecimal sysPropBigDecima(String key, BigDecimal defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toBigDecimal(property);

    }


    public static BigInteger sysPropBigInteger(String key) {
        return sysPropBigInteger(key, (BigInteger) null);
    }


    public static BigInteger sysPropBigInteger(String key, BigInteger defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toBigInteger(property);

    }


    public static <T extends Enum> T sysPropEnum(Class<T> cls, String key) {
        return sysProp(cls, key,  null);
    }

    public static <T extends Enum> T sysProp(Class<T> cls, String key, T defaultValue) {

        String property = (String) systemProperties.get(key);
        if (property == null) {
            property = env.get(key);
        }

        if (property == null) {
            String newKey = Str.underBarCase(key);
            property = env.get(newKey);
        }

        if (property == null) {
            return defaultValue;
        }

        return Conversions.toEnum(cls, property);

    }




    public static String putSysProp(String key, Object value) {
        return (String) systemProperties.put(key, Conversions.toString(value));
    }

    public static boolean hasSysProp(String propertyName) {
        return systemProperties.containsKey(propertyName);
    }

    public static void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }


    public static long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }


    public static long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }


    public static long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }


    static boolean _oracleJVMAndUnix = false;
    static {
        try {
            Class.forName("com.sun.management.UnixOperatingSystemMXBean");
            _oracleJVMAndUnix = true;
        } catch (ClassNotFoundException e) {
            _oracleJVMAndUnix = false;
        }
    }

    private final static boolean oracleJVMAndUnix = _oracleJVMAndUnix;


    public static List<GarbageCollectorMXBean> gc() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    public static double loadAverage() {
        return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    }



    public static long maxFileDescriptorCount() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getMaxFileDescriptorCount();
        }else {
            return -1;
        }
    }


    public static long openFileDescriptorCount() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getOpenFileDescriptorCount();
        }else {
            return -1;
        }
    }


    public static long committedVirtualMemorySize() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getCommittedVirtualMemorySize();
        }else {
            return -1;
        }
    }


    public static long totalSwapSpaceSize() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getTotalSwapSpaceSize();
        }else {
            return -1;
        }
    }


    public static long freeSwapSpaceSize() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getFreeSwapSpaceSize();
        }else {
            return -1;
        }
    }


    public static long processCpuTime() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getProcessCpuTime();
        }else {
            return -1;
        }
    }


    public static long freePhysicalMemorySize() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getFreePhysicalMemorySize();
        }else {
            return -1;
        }
    }


    public static long totalPhysicalMemorySize() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getTotalPhysicalMemorySize();
        }else {
            return -1;
        }
    }



    public static double systemCpuLoad() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getSystemCpuLoad();
        }else {
            return -1;
        }
    }


    public static double processCpuLoad() {

        if (oracleJVMAndUnix) {

            UnixOperatingSystemMXBean unix = (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return unix.getProcessCpuLoad();
        }else {
            return -1;
        }
    }



    public static long uptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public static long startTime() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    public static int pendingFinalizationCount() {
        return ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount();
    }


    public static MemoryUsage heapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }

    public static MemoryUsage nonHeapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
    }


    public static int threadPeakCount() {

        return ManagementFactory.getThreadMXBean().getPeakThreadCount();
    }


    public static int threadCount() {

        return ManagementFactory.getThreadMXBean().getThreadCount();
    }


    public static long threadsStarted() {

        return ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
    }

    public static long threadCPUTime() {

        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

    public static long threadUserTime() {

        return ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
    }


    public static int threadDaemonCount() {

        return ManagementFactory.getThreadMXBean().getDaemonThreadCount();
    }

    public static <T> T loadFromFileLocation(Class<T> clazz, String... fileLocations) {
        for (String fileLocation : fileLocations) {
            if (fileLocation != null && IO.exists(fileLocation)) {
                try {
                    return new JsonParserFactory().create().parseFile(clazz, fileLocation);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    Exceptions.handle(ex, "Unable to read file from ", fileLocation);
                    return null;
                }
            }
        }

        try {
            return clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            Exceptions.handle(e, "Unable to create instance of " + clazz.getName());
            return null;
        }
    }
}
