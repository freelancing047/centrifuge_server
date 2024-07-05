package csi.client.gwt.lib.ace.src;

/**
 * Enumeration for ACE annotation types.
 */
public enum AceAnnotationType {
	ERROR("error"), //$NON-NLS-1$
	INFORMATION("information"), //$NON-NLS-1$
	WARNING("warning"); //$NON-NLS-1$

	private final String name;

	private AceAnnotationType(final String name) {
		this.name = name;
	}

	/**
	 * @return the theme name (e.g., "error")
	 */
	public String getName() {
		return name;
	}
}
