package server.websocket;

import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authToken, Integer gameID, WsContext session) {
        var connection = new Connection(authToken, gameID, session);
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcast(Integer gameID, String excludeAuthToken, ServerMessage message) {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.session.isOpen()) {
                if (c.gameID.equals(gameID) && !c.authToken.equals(excludeAuthToken)) {
                    c.session.send(new Gson().toJson(message));
                }
            } else {
                removeList.add(c);
            }
        }

        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void send(String authToken, ServerMessage message) {
        var c = connections.get(authToken);
        if (c != null && c.session.session.isOpen()) {
            c.session.send(new Gson().toJson(message));
        }
    }
}
