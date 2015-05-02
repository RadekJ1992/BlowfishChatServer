
import java.net.Socket;
import java.util.Vector;

/**
 * Klasa rozdzielająca i przesyłająca dalej przychodzące wiadomości
 * Created by radoslawjarzynka on 05.11.14.
 */
public class ServerDispatcher implements Runnable
{
    //wektor wiadomości
    private Vector messages = new Vector();
    //wektor aplikacji mobilnych
    private Vector clients = new Vector();
    //wektor aplikacji monitorującej
    private Vector servers = new Vector();

    /**
     * Dodanie aplikacji mobilnej do listy
     * @param relayClientInfo
     */
    public synchronized void addClient(RelayClientInfo relayClientInfo)
    {
        clients.add(relayClientInfo);
    }

    /**
     * Dodanie aplikacji monitorującej
     * @param relayClientInfo
     */
    public synchronized void addServer(RelayClientInfo relayClientInfo)
    {
        servers.add(relayClientInfo);
    }

    /**
     * usunięcie aplikacji mobilnej
     * @param relayClientInfo
     */
    public synchronized void deleteClient(RelayClientInfo relayClientInfo)
    {
        int clientIndex = clients.indexOf(relayClientInfo);
        if (clientIndex != -1)
            clients.removeElementAt(clientIndex);
    }

    /**
     * usunięcie serwera
     * @param relayClientInfo
     */
    public synchronized void deleteServer(RelayClientInfo relayClientInfo)
    {
        int clientIndex = servers.indexOf(relayClientInfo);
        if (clientIndex != -1)
            servers.removeElementAt(clientIndex);
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
        messages.add(aMessage);
        notify();
    }

    /**
     * Dodanie nowej wiadomości do listy wiadomości bez logowania
     * @param aMessage
     */
    public synchronized void dispatchMessage(String aMessage)
    {
        messages.add(aMessage);
        notify();
    }

    /**
     * Pobranie pierwszej wiadomości z wektora wszystkich wiadomości
     * @return
     * @throws InterruptedException
     */
    private synchronized String getNextMessageFromQueue()
            throws InterruptedException
    {
        if (messages.size()==0) {
            return null;
        }
        String message = (String) messages.get(0);
        messages.removeElementAt(0);
        return message;
    }

    /**
     * wysłanie wiadomości do wszystkich aplikacji mobilnych
     * @param aMessage
     */
    private synchronized void sendMessageToAllClients(String aMessage)
    {
        for (int i=0; i< clients.size(); i++) {
            RelayClientInfo relayClientInfo = (RelayClientInfo) clients.get(i);
            relayClientInfo.relayClientSender.sendMessage(aMessage);
        }
    }

    /**
     * wysłanie wiadomości do wszystkich aplikacji monitorujących
     * @param aMessage
     */
    private synchronized void sendMessageToAllServers(String aMessage)
    {
        System.out.println("Sending Message : " + aMessage);
        for (int i=0; i< servers.size(); i++) {
            RelayClientInfo relayClientInfo = (RelayClientInfo) servers.get(i);
            relayClientInfo.relayClientSender.sendMessage(aMessage);
        }
    }

    public void run()
    {
        System.out.println("Server Dispatcher started");
        try {
            while (true) {
                if (!servers.isEmpty()) {
                    String message = getNextMessageFromQueue();
                    if (message != null) {
                        sendMessageToAllServers(message);
                    }
                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException ie) {
        }
    }

}
