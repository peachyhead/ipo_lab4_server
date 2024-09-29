package core;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class MediaStorage extends ArrayList<MediaModel> {
    
    private final Action addAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            
        }
    };
    
    @Override
    public boolean add(MediaModel model) {
        var succeed = super.add(model);
        if (succeed)
            addAction.putValue("add", model);
        return succeed;
    }
    
    public void subscribeOnAdd(PropertyChangeListener listener) {
        addAction.addPropertyChangeListener(listener);
    }
}
