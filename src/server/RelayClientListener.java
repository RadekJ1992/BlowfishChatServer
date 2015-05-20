package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Klasa odbierająca wiadomości od klientów
 * Created by radoslawjarzynka on 05.11.14.
 */
public class RelayClientListener extends Thread
{
    private ServerDispatcher serverDispatcher;
    private RelayClientInfo relayClientInfo;
    private BufferedReader bufferedReader;

    public RelayClientListener(RelayClientInfo relayClientInfo, ServerDispatcher serverDispatcher)
            throws IOException
    {
        this.relayClientInfo = relayClientInfo;
        this.serverDispatcher = serverDispatcher;
        Socket socket = relayClientInfo.socket;
        System.out.println("Listener Created for IP " + socket.getInetAddress());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run()
    {
        System.out.println("Listener Started for IP " + relayClientInfo.socket.getInetAddress());
        try {
            while (!isInterrupted()) {
                String message = bufferedReader.readLine();
                if (message == null) {
                    break;
                }
                serverDispatcher.dispatchMessage(relayClientInfo, message);
                Thread.sleep(200);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(relayClientInfo.socket.getInetAddress() + " Disconnected!");
        }

        relayClientInfo.relayClientSender.interrupt();
        serverDispatcher.deleteClient(relayClientInfo);
    }

}
