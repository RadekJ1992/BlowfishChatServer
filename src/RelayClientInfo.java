import java.net.Socket;

/**
 * Klasa przechowująca informacje o kliencie
 * Created by radoslawjarzynka on 05.11.14.
 */
public class RelayClientInfo
{
    public Socket socket = null;
    public RelayClientListener relayClientListener = null;
    public RelayClientSender relayClientSender = null;
}

