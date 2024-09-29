package handler;

import core.MediaModel;
import core.MediaStorage;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static handler.XMLHandler.getXMLFromImage;
import static handler.XMLHandler.saveImageFromXMLData;

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
        if (request.equals(ClientRequestCode.imageSend)) {
            if (onImageSend(args, mediaID)) return;
        }
        if (request.equals(ClientRequestCode.imageUpload)) {
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
        var inline = String.format("%s|%s", ClientRequestCode.clientsUpdate, ids);
        client.get().send(inline);
    }

    private boolean onClientHandshake() throws IOException {
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(clientID)).findFirst();
        if (client.isEmpty()) return true;
        var inline = String.format("%s|%s", ClientRequestCode.clientHandshake, clientID);
        client.get().send(inline);
        return false;
    }

    private boolean onImageUpload(List<String> args, String mediaID) throws IOException {
        var client = handlerStorage.stream()
                .filter(item -> item.getId().equals(clientID)).findFirst();
        if (client.isEmpty()) return true;
        var inline = String.format("%s|%s", ClientRequestCode.imageLoadedOnServer, mediaID);

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
        var data = getXMLFromImage(mediaID);
        var inline = String.format("%s|%s|%s|%s", ClientRequestCode.imageReceive, 
                clientID, mediaID, data);
        client.get().send(inline);
        return false;
    }

    private Stream<MediaModel> resolveMedia(String id) {
        return mediaStorage.stream().filter(item -> item.id().equals(id));
    }
    
    private void setupMedia(String id, String ownerID, String data) {
        var duplicate = mediaStorage.stream()
                .filter(item -> item.path().equals(data)).findFirst();
        if (duplicate.isPresent()) return;

        var media = new MediaModel(id, ownerID, id);
        saveImageFromXMLData(data, id);
        mediaStorage.add(media);
    }
}
