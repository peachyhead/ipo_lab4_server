package handler;

import core.MediaModel;
import core.MediaStorage;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

public class ClientRequestProcessor {
    
    private final String clientID;
    
    private final ClientHandlerStorage handlerStorage;
    private final MediaStorage mediaStorage;

    public ClientRequestProcessor(String id,
                                  ClientHandlerStorage handlerStorage,
                                  MediaStorage mediaStorage) {
        this.clientID = id;
        this.handlerStorage = handlerStorage;
        this.mediaStorage = mediaStorage;
    }

    public void process(String request, List<String> args) throws IOException {
        var mediaID = args.getFirst();
        if (request.equals(ClientRequestCode.visualSend)) {
            if (onImageSend(args, mediaID)) return;
        }
        if (request.equals(ClientRequestCode.visualUpload)) {
            if (onImageUpload(args, mediaID)) return;
        }
        if (request.equals(ClientRequestCode.clientHandshake)) {
            if (onClientHandshake()) return;
        }
        if (request.equals(ClientRequestCode.clientsUpdate)) {
            onClientsUpdate(args);
        }
    }

    private void onClientsUpdate(List<String> args) throws IOException {
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(clientID)).findFirst();
        if (client.isEmpty()) return;
        if (args.isEmpty()) return;
        var ids = new StringBuilder();
        var formatted = args.stream().skip(1).toList();
        for (String id : formatted) {
            ids.append(id).append(",");
        }
        ids.append(args.getFirst());
        var data = String.format("%s|%s", ClientRequestCode.clientsUpdate, ids);
        client.get().send(data);
    }

    private boolean onClientHandshake() throws IOException {
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(clientID)).findFirst();
        if (client.isEmpty()) return true;
        var data = String.format("%s|%s", ClientRequestCode.clientHandshake, clientID);
        client.get().send(data);
        return false;
    }

    private boolean onImageUpload(List<String> args, String mediaID) throws IOException {
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(clientID)).findFirst();
        if (client.isEmpty()) return true;
        var inline = String.format("%s|%s", ClientRequestCode.visualLoadedOnServer, mediaID);

        if (resolveMedia(mediaID).findFirst().isPresent()) {
            client.get().send(inline);
            return true;
        }
        var mediaData = args.stream().skip(1).findFirst();
        if (mediaData.isEmpty()) return true;
        setupMedia(mediaID, clientID, mediaData.get());
        client.get().send(inline);
        return false;
    }

    private boolean onImageSend(List<String> args, String mediaID) throws IOException {
        var receiverID = args.stream().skip(1).findFirst();
        if (receiverID.isEmpty()) return true;
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(receiverID.get())).findFirst();
        if (client.isEmpty()) return true;
        var media = resolveMedia(mediaID).findFirst();
        if (media.isEmpty()) return true;
        var visual = mediaStorage.stream().filter(item -> item.id().equals(mediaID)).findFirst();
        if (visual.isEmpty()) return true;
        var data = String.format("%s|%s|%s|%s", ClientRequestCode.visualReceive, 
                clientID, mediaID, visual.get().data());
        client.get().send(data);
        return false;
    }

    private Stream<MediaModel> resolveMedia(String id) {
        return mediaStorage.stream().filter(item -> item.id().equals(id));
    }
    
    private void setupMedia(String id, String ownerID, String data) {
        var duplicate = mediaStorage.stream()
                .filter(item -> item.data().equals(data)).findFirst();
        if (duplicate.isPresent()) return;

        var media = new MediaModel(id, ownerID, data);
        mediaStorage.add(media);
    }
}
