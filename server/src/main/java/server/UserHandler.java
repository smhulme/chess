package server;

import dataaccess.DataAccessException;
import service.UserService;
import io.javalin.http.Context;
import datamodel.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import dataaccess.ForbiddenException;




public class UserHandler {
    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

            if (userData.username() == null || userData.password() == null) {
                ctx.status(400).json(new ErrorResponse("Error: No username or password given"));
                return;
            }

            RegisterResponse registerResponse = userService.register(userData);
            ctx.status(200).json(registerResponse);
        } catch (ForbiddenException e) {
            ctx.status(403).json(new ErrorResponse("Error: already registered"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Error: Bad Request"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(new ErrorResponse("Malformed JSON"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(Context ctx) {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

            if (userData.username() == null || userData.password() == null) {
                ctx.status(400).json(new ErrorResponse("Error: No username or password given"));
                return;
            }

            RegisterResponse registerResponse = userService.loginUser(userData);
            ctx.status(200).json(registerResponse);
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error: Unauthorized"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(new ErrorResponse("Malformed JSON"));
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            userService.logoutUser(authToken);

            ctx.status(200).json(new Gson().fromJson("{}", Object.class));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class ErrorResponse {
        public final String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
