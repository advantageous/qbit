package io.advantageous.qbit.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by rhightower on 3/4/15.
 */
public class PortUtils {

    public static int useOneOfThesePorts(int... ports)  {
        for (int port : ports) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.close();
                return port;
            } catch (IOException ex) {
                continue; // try next port
            }
        }
        // if the program gets here, no port in the range was found
        throw new IllegalStateException("no free port found");
    }



    public static int useOneOfThePortsInThisRange(int start, int stop)  {
        for (int index = start; index < stop; index++) {
            try {
                ServerSocket serverSocket = new ServerSocket(index);
                serverSocket.close();
                return index;
            } catch (IOException ex) {
                continue; // try next port
            }
        }
        // if the program gets here, no port in the range was found
        throw new IllegalStateException("no free port found");
    }


    public static int findOpenPort()  {
        return useOneOfThePortsInThisRange(6000, 30_000);
    }


    public static int findOpenPortStartAt(int start)  {
        return useOneOfThePortsInThisRange(start, 30_000);
    }
}
