package dataaccess;

import datamodel.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements UserAccess{
    private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public void addUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }


}
