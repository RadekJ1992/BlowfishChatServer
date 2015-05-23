package server;

import manager.DBManager;
import model.Message;
import model.User;
import server.RelayClientInfo;

import java.io.IOException;
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
    private Vector<RelayClientInfo> connectedClients = new Vector<RelayClientInfo>();

    /**
     * Dodanie aplikacji mobilnej do listy
     * @param relayClientInfo
     */
    public synchronized void addClient(RelayClientInfo relayClientInfo)
    {
        for (RelayClientInfo client : connectedClients) {
            if (client.userName != null && client.userName.equals(relayClientInfo.userName)) {
                connectedClients.remove(client);
            }
        }
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

        String[] messageParts = aMessage.split(";");

        String messageType = messageParts[0];

        switch (messageType) {
            case "REG":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String passSha = messageParts[2];
                    if (DBManager.getInstance().getUserByUsername(username) != null) {
                        relayClientInfo.relayClientSender.sendMessage("REG_NOOK");
                        break;
                    }
                    User user = new User(username);
                    user.setPassword(passSha);
                    DBManager.getInstance().addNewUser(user);
                    relayClientInfo.relayClientSender.sendMessage("REG_OK");
                }
                break;
            case "LOGIN":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String passSha = messageParts[2];

                    if (DBManager.getInstance().getUserByUsername(username) != null &&
                            passSha.equals(DBManager.getInstance().getUserByUsername(username).getPassword())) {
                        User u = DBManager.getInstance().getUserByUsername(username);
                        if (u != null) {
                            DBManager.getInstance().generateTokenForUser(u);
                            relayClientInfo.userName = u.getUsername();
                            relayClientInfo.relayClientSender.sendMessage("LOGIN_OK;" + u.getToken());
                        } else {
                            relayClientInfo.relayClientSender.sendMessage("LOGIN_NOOK");
                        }
                    } else {
                        relayClientInfo.relayClientSender.sendMessage("LOGIN_NOOK");
                    }
                }
                break;
            case "GET_CLIENTS":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String token = messageParts[2];

                    if (DBManager.getInstance().isTokenValid(new User(username), token)) {
                        StringBuilder sb = new StringBuilder("CLIENTS");
                        for (String user : DBManager.getInstance().getAllUsers(username)) {
                            sb.append(";" + user);
                        }
                        relayClientInfo.relayClientSender.sendMessage(sb.toString());
                    } else {
                        relayClientInfo.relayClientSender.sendMessage("BAD_TOKEN");
                    }
                }
                break;
            case "SEND":
                if (messageParts.length == 5) {
                    String senderName = messageParts[1];
                    String token = messageParts[2];
                    String receiverName = messageParts[3];
                    String messageString = messageParts[4];
                    if (DBManager.getInstance().isTokenValid(new User(senderName), token)) {
                        User sender = DBManager.getInstance().getUserByUsername(senderName);
                        User receiver = DBManager.getInstance().getUserByUsername(receiverName);
                        Message message = new Message(receiver.getId(), sender.getId(), messageString);
                        DBManager.getInstance().insertNewMessage(message);
                    } else {
                        relayClientInfo.relayClientSender.sendMessage("BAD_TOKEN");
                    }
                }
                break;
            case "LOGOUT":
                if (messageParts.length == 3) {
                    String username = messageParts[1];
                    String token = messageParts[2];
                    try {
                        relayClientInfo.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connectedClients.remove(relayClientInfo);
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
