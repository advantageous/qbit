package io.advantageous.qbit.sample.server;

/**
 * Created by rhightower on 11/5/14.
 */
public class TodoClientMain {

    public static void main(final String... args) {
        if (args.length>0) {
            if (args[0].equalsIgnoreCase("websocket")) {
                TodoWebSocketClient.main(args);
            } else if (args[0].equalsIgnoreCase("rest")) {
                TodoRESTClient.main(args);
            }
        } else {
            TodoWebSocketClient.main(args);
        }
    }

}


