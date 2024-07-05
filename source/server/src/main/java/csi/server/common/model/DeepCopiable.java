package csi.server.common.model;

import java.util.Map;

public interface DeepCopiable<T> {

    T copy(Map<String, Object> copies);
}
