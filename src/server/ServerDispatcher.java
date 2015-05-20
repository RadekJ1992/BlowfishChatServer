package server;

import manager.DBManager;
import model.Message;
import model.User;
import server.RelayClientInfo;

import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

/**
 * Klasa rozdzielająca i przesyłająca dalej przychodzące wiadomości
 * Created by radoslawjarzynka on 05.11.14.
 */
public class ServerDispatcher //implements Runnable
{
    //wektor aplikacji mobilnych
    private Vector connectedClients = new Vector<RelayClientInfo>();

    /**
     * Dodanie aplikacji mobilnej do listy
     * @param relayClientInfo
     */
    public synchronized void addClient(RelayClientInfo relayClientInfo)
    {
        //TODO czy istnieje
        connectedClients.add(relayClientInfo);
    }

    /**
     * usunięcie aplikacji mobilnej
     * @param relayClientInfo
     */
    public synchronized void deleteClient(RelayClientInfo relayClientInfo)
    {
        //TODO
        int clientIndex = connectedClients.indexOf(relayClientInfo);
        if (clientIndex != -1)
            connectedClients.removeElementAt(clientIndex);
    }

    /**
     * Zalogowanie przyjścia wiadomości i dodanie jej do wektora wiadomości
     * @param relayClientInfo
     * @param aMessage
     */
    public synchronized void dispatchMessage(RelayClientInfo relayClientInfo, String aMessage)
    {
        Socket socket = relayClientInfo.socket;
        String senderIP = socket.getInetAddress().getHostAddress();
        String senderPort = "" + socket.getPort();
        System.out.println("Received Message from " + senderIP + ":" + senderPort + " : " + aMessage);

//TODO walidacja tokena
        String[] messageParts = aMessage.split(";");

        String messageType = messageParts[0];

        switch (messageType) {
            case "REG":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String passSha = messageParts[2];
                    //TODO istnieje?
                    User user = new User(username);
                    user.setPassword(passSha);
                    DBManager.getInstance().addNewUser(user);
                }
                break;
            case "LOGIN":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String passSha = messageParts[2];
                    DBManager.getInstance().generateTokenForUser(new User(username));

                    //TODO odeslac token
                }
                break;
            case "GET_CLIENTS":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String token = messageParts[2];
                    //TODO getAllUsers
                }
                break;
            case "SEND":
                if (messageParts.length == 3) {
                    String senderName = messageParts[1];
                    String token = messageParts[2];
                    String receiverName = messageParts[3];
                    String messageString = messageParts[4];
                    User sender = DBManager.getInstance().getUserByUsername(senderName);
                    User receiver = DBManager.getInstance().getUserByUsername(receiverName);
                    Message message = new Message(receiver.getId(), sender.getId(), messageString);
                    DBManager.getInstance().sendMessage(message);
                }
                break;
            case "LOGOUT":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String token = messageParts[2];

                    DBManager.getInstance().removeUserToken(new User(username));
                }
                break;
            default:
                System.out.println("Unknown message type: " + aMessage);
                break;
        }
        notify();
    }
}
