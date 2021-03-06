package org.academiadecodigo.concurrency;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebServer {

    private static final Logger logger = Logger.getLogger(WebServer.class.getName());

    public static final String DOCUMENT_ROOT = "www/";
    public static final int DEFAULT_PORT = 8080;

    private ServerSocket bindSocket = null;
    private ExecutorService cachedPool;

    public static void main(String[] args) {

        try {

            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            WebServer webServer = new WebServer();
            webServer.listen(port);
            webServer.close();

        } catch (NumberFormatException e) {

            System.err.println("Usage: WebServer [PORT]");
            System.exit(1);

        }
    }

    private void listen(int port) {

        try {

            bindSocket = new ServerSocket(port);
            cachedPool = Executors.newCachedThreadPool();

            logger.log(Level.INFO, "server bind to " + getAddress());
            dispatch(bindSocket);

        } catch (IOException e) {

            logger.log(Level.SEVERE, "could not bind to port " + port);
            logger.log(Level.SEVERE, e.getMessage());
            System.exit(1);

        }
    }

    private void dispatch(ServerSocket bindSocket) {

        while (true) {

            try {

                ClientDispatcher clientDispatcher = new ClientDispatcher(bindSocket.accept());

                logger.log(Level.INFO, "new connection from " + clientDispatcher.getAddress());

      
                cachedPool.submit(clientDispatcher);



            } catch (IOException e) {

                logger.log(Level.SEVERE, e.getMessage());
                close();

            }
        }
    }

    private void close() {
        if(bindSocket != null) {
            try{
                try {
                    bindSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(cachedPool != null) {
                cachedPool.shutdown();
            }
        }
    }

    private String getAddress() {

        if (bindSocket == null) {
            return null;
        }

        return bindSocket.getInetAddress().getHostAddress() + ":" + bindSocket.getLocalPort();
    }

}
