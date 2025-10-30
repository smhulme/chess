package dataaccess;

import datamodel.RegisterResponse;

public interface AuthAccess {
    void addAuth(RegisterResponse authData) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    RegisterResponse getAuth(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}