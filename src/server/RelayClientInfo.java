package server;

import java.net.Socket;
import java.util.Objects;

/**
 * Klasa przechowująca informacje o kliencie
 * Created by radoslawjarzynka on 05.11.14.
 */
public class RelayClientInfo
{
    public String userName = null;
    public Socket socket = null;
    public RelayClientListener relayClientListener = null;
    public RelayClientSender relayClientSender = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelayClientInfo)) return false;
        RelayClientInfo that = (RelayClientInfo) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(socket, that.socket) &&
                Objects.equals(relayClientListener, that.relayClientListener) &&
                Objects.equals(relayClientSender, that.relayClientSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, socket, relayClientListener, relayClientSender);
    }
}

