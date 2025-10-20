package dataaccess;

import datamodel.UserData;

public interface DataAccess {
    void addUser(UserData user);
    UserData getUser(String username);
    void clear();
}
