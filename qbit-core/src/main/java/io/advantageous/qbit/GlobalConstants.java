package io.advantageous.qbit;

public class GlobalConstants {

    public final static int BATCH_SIZE = Integer.valueOf(System.getProperty("io.advantageous.qbit.BATCH_SIZE", "1000"));

    public final static int POLL_WAIT = Integer.valueOf(System.getProperty("io.advantageous.qbit.POLL_WAIT", "15"));

    public final static boolean DEBUG = Boolean.valueOf(System.getProperty("io.advantageous.qbit.DEBUG", "false"));

    public final static int  NUM_BATCHES = Integer.valueOf(System.getProperty("io.advantageous.qbit.NUM_BATCHES", "100000"));

}
