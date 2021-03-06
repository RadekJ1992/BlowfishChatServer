package server;

import manager.DBManager;
import model.Message;
import model.User;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

/**
 * Aplikacja wysyłająca wiadomości do klientów
 *
 * Created by radoslawjarzynka on 05.11.14.
 */
public class RelayClientSender extends Thread
{
    private Vector messages = new Vector();

    private ServerDispatcher serverDispatcher;
    private RelayClientInfo relayClientInfo;
    private PrintWriter printWriter;

    public RelayClientSender(RelayClientInfo relayClientInfo, ServerDispatcher serverDispatcher)
            throws IOException
    {
        this.relayClientInfo = relayClientInfo;
        this.serverDispatcher = serverDispatcher;
        Socket socket = relayClientInfo.socket;
        System.out.println("Sender Created for IP " + relayClientInfo.socket.getInetAddress());
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * wysłanie wiadomości
     * @param aMessage
     */
    public synchronized void sendMessage(String aMessage)
    {
        messages.add(aMessage);
        notify();
    }

    /**
     * Pobranie pierwszego elementu z listy wiadomości
     * @return
     * @throws InterruptedException
     */
    private synchronized String getNextMessageFromQueue() throws InterruptedException
    {
        while (messages.size()==0) {
            return null;
        }
        //TODO pobranie z bazy wiadomości z tym userem i z flaga sent = 0;
        String message = (String) messages.get(0);
        messages.removeElementAt(0);
        return message;
    }

    /**
     * wysłanie wiadomości
     * @param aMessage
     */
    private void sendMessageToClient(String aMessage)
    {
        printWriter.println(aMessage);
        printWriter.flush();
    }

    public void run()
    {
        System.out.println("Sender Started for IP " + relayClientInfo.socket.getInetAddress());
        try {
            while (!isInterrupted()) {
                String message = getNextMessageFromQueue();
                if (message != null) {
                    sendMessageToClient(message);
                }
                if (relayClientInfo.userName != null) {
                    List<Message> dbMessages = DBManager.getInstance().getUserMessages(new User(relayClientInfo.userName));

                    for (Message msg : dbMessages) {
                        User sender = DBManager.getInstance().getUserById(msg.getSenderId());
                        sendMessageToClient("MSG;" + sender.getUsername() + ";" + msg.getMessage());
                        DBManager.getInstance().sendMessage(msg);
                    }
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            try {
                relayClientInfo.socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            relayClientInfo.userName = null;
            e.printStackTrace();
        } finally {
            System.out.println(relayClientInfo.socket.getInetAddress() + " Disconnected");
        }

        relayClientInfo.relayClientListener.interrupt();
        serverDispatcher.deleteClient(relayClientInfo);
    }

}
