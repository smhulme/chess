package server.websocket;

import io.javalin.websocket.WsContext;

public class Connection {
    public String authToken;
    public Integer gameID;
    public WsContext session;

    public Connection(String authToken, Integer gameID, WsContext session) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.session = session;
    }
}
