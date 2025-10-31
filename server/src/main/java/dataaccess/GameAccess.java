package dataaccess;

import datamodel.GameData;
import java.util.HashSet;

public interface GameAccess {
    HashSet<GameData> listGames() throws DataAccessException;
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException, BadRequestException;
    boolean gameExists(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
}