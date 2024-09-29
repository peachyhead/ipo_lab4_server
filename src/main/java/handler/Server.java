package handler;

import core.MediaStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.UUID;

public class Server {
    
    private static final int Port = 12345;

    private final MediaStorage mediaStorage;
    private final ClientHandlerStorage handlers;

    public Server(ClientHandlerStorage handlers, MediaStorage mediaStorage) {
        this.handlers = handlers;
        this.mediaStorage = mediaStorage;
    }

    public void initialize() {
        handlers.subscribeOnRemove(evt -> updateClientsInfo());
        
        try (ServerSocket serverSocket = new ServerSocket(Port)){
            while (!Thread.currentThread().isInterrupted()) {
                var socket = serverSocket.accept();
                var id = UUID.randomUUID().toString();
                System.out.printf("Client %s connected\n", id);
                var handler = new ClientHandler(id, socket, handlers, mediaStorage);
                handlers.add(handler);
                handler.initialize();
                handler.processInput(ClientRequestCode.clientHandshake, 
                        Collections.singletonList(id));
                updateClientsInfo();
            }
        }
        catch (Exception ignored) {
            
        }
    }

    private void updateClientsInfo() {
        for (ClientHandler clientHandler : handlers) {
            try {
                clientHandler.processInput(ClientRequestCode.clientsUpdate, 
                        handlers.aggregate());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
