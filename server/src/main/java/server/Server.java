package server;

import com.google.gson.Gson;
import io.javalin.*;

import javax.naming.Context;
import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", ctx -> ctx.result("{ \"username\":\"\", \"authToken\":\"\" }"));
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var req = serializer.fromJson(ctx.body(), Map.class);
        // call service to register
        var res = Map.of("username", req.get("username"), "authToken", "xyz");
        ctx.result(serializer.toJson(res));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
