package display.signature;

import deterministic.SimpleGraph;

public class GraphSelectionEvent {
    
    public SimpleGraph selected;
    
    public GraphSelectionEvent(SimpleGraph selected) {
        this.selected = selected;
    }

}
