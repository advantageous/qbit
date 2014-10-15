package io.advantageous.qbit;

public class GlobalConstants {

    public static int BATCH_SIZE = 50;

    public static int POLL_WAIT = 5;

    public static boolean DEBUG = Boolean.valueOf(System.getProperty("org.qbit.DEBUG", "false"));

}
