package client;

public class ResponseException extends Exception {
    final int statusCode;

    public ResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}