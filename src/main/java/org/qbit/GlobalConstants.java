package org.qbit;


import org.boon.core.Sys;

public class GlobalConstants {

    public static int BATCH_SIZE = 50;

    public static int POLL_WAIT = 5;


    public static boolean DEBUG = Sys.sysProp("org.qbit.DEBUG", true);



}
