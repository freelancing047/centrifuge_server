package csi.server.business.visualization.map.cacheloader.pointmap.typesizevalue;

import java.util.Set;

import com.google.gwt.thirdparty.guava.common.collect.Sets;

public class CountDistinctTypeSizeValue implements TypeSizeValue {
	private Set<String> values = Sets.newTreeSet();
	
	public void addValue(String value) {
		if (value == null)
			values.add("__null__value__");
		else
			values.add(value);
	}

	@Override
	public double getValue() {
		return values.size();
	}
}
