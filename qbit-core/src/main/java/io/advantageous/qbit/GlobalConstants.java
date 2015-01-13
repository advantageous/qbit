package io.advantageous.qbit;

public class GlobalConstants {

    public static int BATCH_SIZE = Integer.valueOf(System.getProperty("io.advantageous.qbit.BATCH_SIZE", "50"));

    public static int POLL_WAIT = Integer.valueOf(System.getProperty("io.advantageous.qbit.POLL_WAIT", "5"));

    public static boolean DEBUG = Boolean.valueOf(System.getProperty("io.advantageous.qbit.DEBUG", "false"));

}
