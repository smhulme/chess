package dataaccess;

import datamodel.UserData;

public interface UserAccess {
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData username) throws DataAccessException;
    boolean authenticateUser(String username, String password) throws DataAccessException;
    void clear() throws DataAccessException;
}
