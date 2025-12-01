package client.websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;
import chess.ChessMove;

import jakarta.websocket.*;
import java.net.URI;
import java.io.IOException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME ->
                            notificationHandler
                                    .notify(new Gson().fromJson(message, ServerMessage.LoadGameMessage.class));
                        case ERROR ->
                            notificationHandler.notify(new Gson().fromJson(message, ServerMessage.ErrorMessage.class));
                        case NOTIFICATION ->
                            notificationHandler
                                    .notify(new Gson().fromJson(message, ServerMessage.NotificationMessage.class));
                    }
                }
            });
        } catch (DeploymentException | IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, Integer gameID) throws Exception {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws Exception {
        try {
            var action = new MakeMoveCommand(authToken, gameID, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    public void leave(String authToken, Integer gameID) throws Exception {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }

    public void resign(String authToken, Integer gameID) throws Exception {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException e) {
            throw new Exception("500: " + e.getMessage());
        }
    }
}
