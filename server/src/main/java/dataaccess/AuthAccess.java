package dataaccess;

import datamodel.RegisterResponse;

public interface AuthAccess {
    void addAuth(RegisterResponse authData);

    void deleteAuth(String authToken);

    RegisterResponse getAuth(String authToken) throws DataAccessException;

    void clear();
}
