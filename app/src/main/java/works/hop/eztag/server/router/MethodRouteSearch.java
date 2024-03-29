package works.hop.eztag.server.router;

import works.hop.eztag.server.handler.IReqHandler;

import java.util.EnumMap;
import java.util.Map;

public class MethodRouteSearch implements RouteSearch {

    Map<MethodType, RouteSearch> handlers = new EnumMap<>(MethodType.class);

    public MethodRouteSearch() {
        for (MethodType type : MethodType.values()) {
            handlers.put(type, new PathRouteSearch());
        }
    }

    @Override
    public void store(String method, String path, IReqHandler handler) {
        MethodType type = MethodType.valueOf(method.toUpperCase());
        handlers.get(type).store(method, path, handler);
    }

    @Override
    public IReqHandler fetch(String method, String path) {
        MethodType type = MethodType.valueOf(method.toUpperCase());
        return handlers.get(type).fetch(method, path);
    }

    public enum MethodType {POST, GET, PUT, DELETE, PATCH}
}
