package service;

import dataaccess.*;
import datamodel.*;

import java.util.HashSet;
import java.util.Random;

public class GameService {
    GameAccess gameAccess;
    AuthAccess authAccess;

    public GameService(GameAccess gameAccess, AuthAccess authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        return gameAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        Random rand = new Random();
        int gameID;
        do {
            gameID = rand.nextInt(9999) + 1; 
        } while (gameAccess.gameExists(gameID)); 

        gameAccess.createGame(new GameData(gameID, null, null, gameName, null));

        return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {
        



        return true;
    }
}
