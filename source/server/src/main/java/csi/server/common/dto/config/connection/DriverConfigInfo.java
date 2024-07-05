package csi.server.common.dto.config.connection;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.exception.CentrifugeException;


public class DriverConfigInfo implements IsSerializable {

    private List<ConfigItem> configItems = new ArrayList<ConfigItem>();

    /*
     * Validate that:
     * 1) The config items all have keys
     * 2) All config item keys are unique.
     */
    public void validate(String key) throws CentrifugeException {
        Set<String> uiKeys = new HashSet<String>();
        if (configItems != null) {
            for (ConfigItem item : configItems) {
                if (item.getKey() == null || "".equals(item.getKey())) {
                    throw new CentrifugeException("Driver " + key + " contains a configuration item with no key");
                }

                if (uiKeys.contains(item.getKey())) {
                    throw new CentrifugeException("Driver " + key + " contains multiple configuration items with key "
                            + item.getKey());
                }
                uiKeys.add(item.getKey());
            }
        }
    }

    public List<ConfigItem> getConfigItems() {
        return configItems;
    }

    public void setConfigItems(List<ConfigItem> configItems) {
        this.configItems = configItems;
    }

}