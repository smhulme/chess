package service;

import java.util.UUID;

import dataaccess.AuthAccess;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserAccess;
import datamodel.RegisterResponse;
import datamodel.UserData;

public class UserService {
    UserAccess userAccess;
    AuthAccess authAccess;

    public UserService(UserAccess userAccess, AuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public RegisterResponse register(UserData user) throws BadRequestException {
        try {
            userAccess.createUser(user);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        String authToken = UUID.randomUUID().toString();
        RegisterResponse registerResponse = new RegisterResponse(user.username(), authToken);
        authAccess.addAuth(registerResponse);

        return registerResponse;
    }

    public RegisterResponse loginUser(UserData userData) throws UnauthorizedException {
        boolean userAuth = false;
        try {
            userAuth = userAccess.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        if (userAuth) {
            String authToken = UUID.randomUUID().toString();
            RegisterResponse registerResponse = new RegisterResponse(userData.username(), authToken);
            authAccess.addAuth(registerResponse);
            return registerResponse;
        } else {
            throw new UnauthorizedException();
        }
    }

    public void logoutUser(String authToken) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() {
        userAccess.clear();
        authAccess.clear();
    }
}
