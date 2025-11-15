package client;

import com.google.gson.Gson;
import datamodel.RegisterResponse;
import datamodel.GameData;
import datamodel.UserData;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResponse register(UserData user) throws ResponseException {
        var path = "/user";
        return makeRequest("POST", path, user, RegisterResponse.class);
    }

    public RegisterResponse login(UserData user) throws ResponseException {
        var path = "/session";
        return makeRequest("POST", path, user, RegisterResponse.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        makeRequest("DELETE", path, null, null, authToken);
    }

    public GameData createGame(GameData game, String authToken) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, game, GameData.class, authToken);
    }

    public GameListResult listGames(String authToken) throws ResponseException {
        var path = "/game";
        return makeRequest("GET", path, null, GameListResult.class, authToken);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var path = "/game";
        var body = Map.of("playerColor", playerColor == null ? "" : playerColor, "gameID", gameID);
        makeRequest("PUT", path, body, null, authToken);
    }

    public void clear() throws ResponseException {
        var path = "/db";
        // Clears the database. This request has no body or auth token.
        makeRequest("DELETE", path, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        return makeRequest(method, path, request, responseClass, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authHeader) throws ResponseException {
        try {
            URL url = URI.create(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);

            // Only enable output if we have a request
            http.setDoOutput(request != null);

            if (authHeader != null) {
                http.addRequestProperty("authorization", authHeader);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String message = "Failure: " + status;
            InputStream err = http.getErrorStream();
            if (err != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(err))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    if (sb.length() > 0) message += " - " + sb.toString().trim();
                } catch (IOException ignored) {}
            }
            throw new ResponseException(status, message);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        if (responseClass == null) return null;

        try (InputStream respBody = http.getInputStream()) {
            if (respBody == null) return null;
            InputStreamReader reader = new InputStreamReader(respBody);
            return new Gson().fromJson(reader, responseClass);
        } catch (IOException ioe) {
            InputStream err = http.getErrorStream();
            if (err != null) {
                try (InputStreamReader reader = new InputStreamReader(err)) {
                    return new Gson().fromJson(reader, responseClass);
                } catch (Exception ignore) {
                    return null;
                }
            }
            return null;
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    // Helper record to match the server's JSON response for listGames
    record GameListResult(Collection<GameData> games) {}
}