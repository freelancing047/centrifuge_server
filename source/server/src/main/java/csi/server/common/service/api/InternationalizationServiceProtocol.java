package csi.server.common.service.api;

import java.util.Map;

import csi.shared.gwt.vortex.VortexService;

public interface InternationalizationServiceProtocol extends VortexService {
    public Map<String, String> getProperties(String localeName);
    public String getBestPattern(String localeName, String subformat);
}
