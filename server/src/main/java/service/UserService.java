package service;

import java.sql.SQLException;
import java.util.UUID;

import dataaccess.*;
import datamodel.RegisterResponse;
import datamodel.UserData;

public class UserService {
    UserAccess userAccess;
    AuthAccess authAccess;

    public UserService(UserAccess userAccess, AuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public RegisterResponse register(UserData user) throws BadRequestException, ForbiddenException, DataAccessException {
        // Input validation
        if (user == null ||
            user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required registration fields");
        }

        try {
            userAccess.createUser(user);
        } catch (DataAccessException e) {
            // Check for "user already exists" FIRST (before checking SQLException)
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                throw new ForbiddenException("User already registered");
            }
            // Then check if this is a database connection error (SQLException)
            if (e.getCause() instanceof SQLException) {
                throw e;  // Let database connection errors propagate as 500
            }
            // Any other DataAccessException
            throw e;
        }
        String authToken = UUID.randomUUID().toString();
        RegisterResponse registerResponse = new RegisterResponse(user.username(), authToken);
        authAccess.addAuth(registerResponse);

        return registerResponse;
    }

    public RegisterResponse loginUser(UserData userData) throws UnauthorizedException, DataAccessException {
        boolean userAuth = false;
        try {
            userAuth = userAccess.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            // If it's a SQLException, it's a database connection issue
            if (e.getCause() instanceof SQLException) {
                throw e;
            }
            // Otherwise it's "user not found" - convert to 401
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

    public void logoutUser(String authToken) throws UnauthorizedException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            // If it's a SQLException, it's a database connection issue
            if (e.getCause() instanceof SQLException) {
                throw e;
            }
            // Otherwise it's "auth token not found" - convert to 401
            throw new UnauthorizedException();
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        userAccess.clear();
        authAccess.clear();
    }
}
