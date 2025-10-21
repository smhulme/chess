package dataaccess;

import java.util.HashSet;

import datamodel.GameData;

public class MemoryGameAccess implements GameAccess{
    
    HashSet<GameData> db;

    public MemoryGameAccess() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public HashSet<GameData> listGames() {
        return db;
    }

    @Override
    public void createGame(GameData game) {
        db.add(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game not found, id: " +gameID);
    }

    @Override
    public boolean gameExists(int gameID) {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateGame(GameData game) {
        try {
            db.remove(getGame(game.gameID()));
            db.add(game);
        } catch (DataAccessException e) {
            db.add(game);
        }
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}
