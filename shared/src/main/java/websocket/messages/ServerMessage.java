package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage that)) {
            return false;
        }
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }

    public static class LoadGameMessage extends ServerMessage {
        private final chess.ChessGame game;

        public LoadGameMessage(chess.ChessGame game) {
            super(ServerMessageType.LOAD_GAME);
            this.game = game;
        }

        public chess.ChessGame getGame() {
            return game;
        }
    }

    public static class ErrorMessage extends ServerMessage {
        private final String errorMessage;

        public ErrorMessage(String errorMessage) {
            super(ServerMessageType.ERROR);
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class NotificationMessage extends ServerMessage {
        private final String message;

        public NotificationMessage(String message) {
            super(ServerMessageType.NOTIFICATION);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
