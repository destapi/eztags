package works.hop.eztag.parser;

import works.hop.eztag.pubsub.JEvent;
import works.hop.eztag.pubsub.JReceiver;
import works.hop.eztag.pubsub.JSubscribe;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JContext implements JObserver {

    final JReceiver receiver;
    final Object context;
    final Map<String, List<JNode>> observables = new ConcurrentHashMap<>();

    public JContext(Object context) {
        this(context, null);
    }

    public JContext(Object context, JReceiver receiver) {
        this.receiver = receiver;
        this.context = context;
    }

    public String process(JElement element) {
        element.setContext(context);
        return element.render();
    }

    @Override
    public Map<String, List<JNode>> observables() {
        return this.observables;
    }

    @Override
    public JReceiver receiver() {
        return this.receiver;
    }

    @Override
    public void subscribe(List<JSubscribe> interests) {
        interests.forEach(sub -> {
            for(String interest : sub.getInterests()) {
                if (!observables().containsKey(interest)) {
                    observables().put(interest, new LinkedList<>());
                }
                observables().get(interest).add(sub.getTarget());
                sub.setUnsubscribe(() -> observables().get(interest).remove(sub.getTarget()));
            }
        });
    }

    @Override
    public void addItemToCollection(Object target, String path, Object value) {
        List<JNode> listeners = observables.get("add");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("addItemToCollection");
            node.bubble(event, null, value);
        }
    }

    @Override
    public void removeItemFromCollection(Object target, String path, Object value) {
        List<JNode> listeners = observables.get("remove");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("removeItemFromCollection");
            node.bubble(event, value, null);
        }
    }

    @Override
    public void updateItemInCollection(Object target, String path, int index, Object prev, Object value) {
        List<JNode> listeners = observables.get("update");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("updateItemInCollection");
            node.bubble(event, prev, value);
        }
    }

    @Override
    public void updateItemInCollection(Object target, String path, Object prev, Object value) {
        List<JNode> listeners = observables.get("update");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("updateItemInCollection");
            node.bubble(event, prev, value);
        }
    }

    @Override
    public void addItemToDictionary(Object target, String path, String key, Object oldValue, Object newValue) {
        List<JNode> listeners = observables.get("add");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("addItemToDictionary");
            node.bubble(event, oldValue, newValue);
        }
    }

    @Override
    public void updateItemInDictionary(Object target, String path, String key, Object prev, Object value) {
        List<JNode> listeners = observables.get("update");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("updateItemInDictionary");
            node.bubble(event, prev, value);
        }
    }

    @Override
    public void removeItemFromDictionary(Object target, String path, String key, Object value) {
        List<JNode> listeners = observables.get("delete");
        for (JNode node : listeners) {
            JEvent event = new JEvent();
            event.setSource(node);
            event.setEvent("removeItemFromDictionary");
            node.bubble(event, value, null);
        }
    }
}