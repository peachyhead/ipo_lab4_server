package handler;

import core.MediaStorage;
import lombok.Getter;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientHandler {
    
    @Getter private final String id;

    private final Socket socket;
    private final MediaStorage mediaStorage;
    private final ClientHandlerStorage handlerStorage;
    
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    
    private Thread updateThread;
    
    public ClientHandler(String id, Socket socket,
                         ClientHandlerStorage handlerStorage, 
                         MediaStorage mediaStorage) throws IOException {
        this.id = id;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.mediaStorage = mediaStorage;
        this.handlerStorage = handlerStorage;
    }
    
    public void initialize() {
        updateThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (inputStream.available() > 0) {
                        var inline = inputStream.readUTF();
                        var args = Arrays.stream(inline.split("\\|")).toList();
                        if (args.isEmpty()) return;
                        processInput(args.stream().findFirst().get(),
                                args.stream().skip(1).toList());
                    }
                }
            } catch (IOException ignored) {
                
            } finally {
                handlerStorage.remove(this);
                try {
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        updateThread.start();
    }
    
    public void send(String data) throws IOException {
        outputStream.writeUTF(data);
        System.out.printf("Sent %s to %s\n", data, id);
    }
    
    public void processInput(String eventID, List<String> data) throws IOException {
        var processor = new ClientRequestProcessor(id, handlerStorage, mediaStorage);
        processor.process(eventID, data);
        System.out.printf("Received %s from %s\n", eventID, id);
    }
}
