package ui;

import core.MediaStorage;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    
    private MediaPanel mediaPanel;
    
    public MainFrame() {
        setVisible(true);
        setTitle("Unga Bunga's Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new GridBagLayout());
    }

    public void setupPanel(MediaStorage mediaStorage) {
        mediaPanel = new MediaPanel(mediaStorage);
        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 10, 10, 10);
        
        add(mediaPanel, c);
        mediaPanel.initialize();
    }
}
