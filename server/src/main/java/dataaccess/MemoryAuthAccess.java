package dataaccess;

import datamodel.RegisterResponse;
import java.util.HashSet;
public class MemoryAuthAccess implements AuthAccess {
    
    HashSet<RegisterResponse> db;

    public MemoryAuthAccess() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public void addAuth(RegisterResponse authData) {
        db.add(authData);
    }

    @Override
    public void deleteAuth(String authToken) {
        for (RegisterResponse authData : db) {
            if (authData.authToken().equals(authToken)) {
                db.remove(authData);
                break;
            }
        }
    }

    @Override
    public RegisterResponse getAuth(String authToken) throws DataAccessException {
        for (RegisterResponse authData : db) {
            if (authData.authToken().equals(authToken)) {
                return authData;
            }
        }
        throw new DataAccessException("Auth Token does not exist: " + authToken);
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}
