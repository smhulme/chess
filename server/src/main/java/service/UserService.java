package service;

import dataaccess.UserAccess;
import datamodel.RegisterResponse;
import datamodel.UserData;

public class UserService {
    private final UserAccess dataAccess;
    public UserService(UserAccess dataAccess){
        this.dataAccess = dataAccess;
    }
    public RegisterResponse register(UserData user) throws Exception {
        var existingUser = dataAccess.getUser(user.username());
            if (existingUser != null) {
                throw new Exception("check petshop");
            }
        return new RegisterResponse(user.username(), "xyz");
    }
}
