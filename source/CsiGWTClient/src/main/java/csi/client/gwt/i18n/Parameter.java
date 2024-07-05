package csi.client.gwt.i18n;

public class Parameter {
	private String name;
	private String qualifiedSourceName;

	public Parameter(String name, String qualifiedSourceName) {
		this.name = name;
		this.qualifiedSourceName = qualifiedSourceName;
	}

	public String getName() {
		return name;
	}

	public String getQualifiedSourceName() {
		return qualifiedSourceName;
	}
}
