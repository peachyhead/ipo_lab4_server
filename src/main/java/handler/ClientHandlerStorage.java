package handler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ClientHandlerStorage extends ArrayList<ClientHandler> {

    private Action onRemove = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            
        }
    };
    
    @Override
    public boolean remove(Object o) {
        var handler = (ClientHandler) o;
        var succeed = super.remove(o);
        onRemove.putValue("remove", handler.getId());
        return succeed;
    }

    public List<String> aggregate() {
        var result = new ArrayList<String>();
        for (ClientHandler handler : this) {
            result.add(handler.getId());
        }
        return result;
    }
    
    public void subscribeOnRemove(PropertyChangeListener listener) {
        onRemove.addPropertyChangeListener(listener);
    }
}
