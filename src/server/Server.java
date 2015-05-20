package server; /**
 * created on 19:59:40 3 lis 2014 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Główna klasa aplikacji przekaźnika
 */
public class Server {

    public static final int PORT = 8000;
    private ServerDispatcher serverDispatcher = new ServerDispatcher();

    /**
     * Rozpoczęcie pracy serwera
     */
    public void StartServer() {
        // Open server socket for listening
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
        } catch (IOException se) {
            System.err.println("Can not start listening on ports " + PORT);
            se.printStackTrace();
            System.exit(-1);
        }

        //Thread dispatcherThread = new Thread(serverDispatcher);
        //dispatcherThread.start();
        System.out.println("Created Handler for Clients");
        Handler handlerForClients = new Handler(serverSocket, serverDispatcher);
        Thread clientsHandlerThread = new Thread (handlerForClients);

        clientsHandlerThread.start();
    }

    /**
     * punkt wejścia aplikacji
     * @param args
     */
    public static void main(String[] args) {
        server.Server server = new server.Server();
        server.StartServer();
    }

    /**
     * Wewnętrzna klasa obsługująca klientów przekaźnika
     */
    private class Handler implements Runnable {

        private ServerSocket relayClientSocket;
        private ServerDispatcher serverDispatcher;

        public Handler(ServerSocket relayClientSocket, ServerDispatcher serverDispatcher) {
            this.relayClientSocket = relayClientSocket;
            this.serverDispatcher = serverDispatcher;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    System.out.println("Socket Opened");
                    Socket socket = relayClientSocket.accept();
                    RelayClientInfo relayClientInfo = new RelayClientInfo();
                    relayClientInfo.socket = socket;
                    RelayClientListener relayClientListener =
                            new RelayClientListener(relayClientInfo, serverDispatcher);
                    RelayClientSender relayClientSender =
                            new RelayClientSender(relayClientInfo, serverDispatcher);
                    relayClientInfo.relayClientListener = relayClientListener;
                    relayClientInfo.relayClientSender = relayClientSender;
                    relayClientListener.start();
                    relayClientSender.start();
                    serverDispatcher.addClient(relayClientInfo);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}

