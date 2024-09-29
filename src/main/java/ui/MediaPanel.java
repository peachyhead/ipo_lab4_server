package ui;

import core.MediaModel;
import core.MediaStorage;

import javax.swing.*;
import java.awt.*;

public class MediaPanel extends JPanel {
    
    private final MediaStorage mediaStorage;

    public MediaPanel (MediaStorage mediaStorage) {
        this.mediaStorage = mediaStorage;
        var layout = new GridLayout();
        layout.setColumns(4);
        layout.setVgap(50);
        layout.setHgap(50);
        setLayout(layout);
        setBackground(Color.GRAY);
    }
    
    public void initialize() {
        mediaStorage.subscribeOnAdd(evt -> {
            var media = (MediaModel) evt.getNewValue();
            var view = new MediaView(media);
            add(view);
        });
    }
}
