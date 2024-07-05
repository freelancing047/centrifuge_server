package csi.client.gwt.lib.ace.src;

/**
 * Enumeration for ACE command types.
 */
public enum AceCommand {
	FIND("find"), //$NON-NLS-1$
	FIND_NEXT("findnext"), //$NON-NLS-1$
	FIND_PREVIOUS("findprevious"), //$NON-NLS-1$
	GOTO_LINE("gotoline"), //$NON-NLS-1$
	REPLACE("replace"), //$NON-NLS-1$
	REPLACE_ALL("replaceall"); //$NON-NLS-1$

	private final String name;

	private AceCommand(final String name) {
		this.name = name;
	}

	/**
	 * @return the theme name (e.g., "error")
	 */
	public String getName() {
		return name;
	}
}
