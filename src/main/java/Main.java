import core.MediaStorage;
import handler.ClientHandlerStorage;
import handler.Server;
import ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        var handlerStorage = new ClientHandlerStorage();
        var mediaStorage = new MediaStorage();
        
        var mainFrame = new MainFrame();
        mainFrame.setupPanel(mediaStorage);
        
        var server = new Server(handlerStorage, mediaStorage);
        server.initialize();
    }
}