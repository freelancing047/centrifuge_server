package csi.server.business.visualization.graph.optionset;

import java.util.ArrayList;
import java.util.Map;

public class OptionSet {

    public static final String LINK_TYPE = "LinkType";
    public static final String NODE_TYPE = "NodeType";
    public String name;
    public String IconScale = ".75";
    public String comment;
    public Map<String, Map<String, Options>> optionMap;
    public String bgcolor;
    public String bundleThreshold;
	public ArrayList<String> shapes = new ArrayList<String>();
	public String bundleIcon;
	public String bundleShape = "none";
	public String bundleColor = "0";
	private final String DEFAULT_BUNDLE_ICON = "/Baseline/Communications/Package.png";
	public String bundleOverlayScale = ".55";
			

    public Options getOptions(String optionType, String typeName) {
        Map<String, Options> optionTypeMap = optionMap.get(optionType);
        if (optionTypeMap == null) {
            return null;
        }

        if (typeName != null) {
            return optionTypeMap.get(typeName);
        } else {
            return optionTypeMap.values().iterator().next();
        }
    }

    public String getOption(String optionType, String typeName, String attribute) {
        Options options = getOptions(optionType, typeName);
        if (options == null) {
            return null;
        }
        return options.properties.get(attribute);
    }

    public String getNodeTypeOption(String typeName, String attribute) {
        return getOption(NODE_TYPE, typeName, attribute);
    }

    public String getLinkTypeOption(String typeName, String attribute) {
        return getOption(LINK_TYPE, typeName, attribute);
    }
    
    public String getBundleIcon(){
    	return bundleIcon==null ? DEFAULT_BUNDLE_ICON: name+"/"+bundleIcon;
    }

}
