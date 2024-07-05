package csi.server.business.visualization.graph.optionset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class OptionSetConverter implements Converter {

	@Override
	public boolean canConvert(Class type) {
		return type.equals(OptionSet.class);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		OptionSet optset = (OptionSet) obj;
		if (optset.name != null) {
			writer.addAttribute("name", optset.name);
		}

		if (optset.bgcolor != null) {
			writer.addAttribute("RGbgcolor", optset.bgcolor);
		}

		writer.startNode("Comment");
		writer.setValue(optset.comment);
		writer.endNode();

		Map<String, Map<String, Options>> optionMap = optset.optionMap;
		Set<String> types = optionMap.keySet();
		for (String type : types) {
			Map<String, Options> map = optionMap.get(type);
			Collection<Options> values = map.values();
			for (Options option : values) {
				writer.startNode(type);
				context.convertAnother(option);
				writer.endNode();
			}

		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		OptionSet optset = new OptionSet();
		optset.optionMap = new HashMap<String, Map<String, Options>>();
		
		optset.name = reader.getAttribute("name");
		optset.bgcolor = reader.getAttribute("RGbgcolor");
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			String nodeName = reader.getNodeName();
			if (nodeName.equals("Comment")) {
				optset.comment = reader.getValue();
			} else if (nodeName.equals("BundleThreshold")) {
				optset.bundleThreshold = reader.getValue();
			} else if(nodeName.equals("IconScale")){
				optset.IconScale = reader.getValue();
			} else if(nodeName.equals("Shapes")){
				while(reader.hasMoreChildren()){
					reader.moveDown();
					optset.shapes.add(reader.getValue());//this can't be right?
					reader.moveUp();
				}
			} else if(nodeName.equals("BundleNode")){
				optset.bundleIcon = reader.getAttribute("icon");
				optset.bundleShape = reader.getAttribute("shape");
				optset.bundleColor = reader.getAttribute("color");
				optset.bundleOverlayScale = reader.getAttribute("overlayScale");
			} else {

				Options option = (Options) context.convertAnother(optset, Options.class);
				Map<String, Options> map = optset.optionMap.get(nodeName);
				if (map == null) {
					map = new HashMap<String, Options>();
					optset.optionMap.put(nodeName, map);
				}
				map.put(option.key, option);
			}
			reader.moveUp();
		}
		return optset;
	}

}
