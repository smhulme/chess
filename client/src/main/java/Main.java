import client.ServerFacade;
import ui.REPL;

public class Main {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length > 0 && args[0] != null && !args[0].isBlank()) {
            serverUrl = args[0];
        }
        
        ServerFacade facade = new ServerFacade(serverUrl);
        REPL ui = new REPL(facade);
        ui.run();
    }
}