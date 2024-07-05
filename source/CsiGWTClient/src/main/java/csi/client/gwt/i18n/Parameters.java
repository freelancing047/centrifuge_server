package csi.client.gwt.i18n;

public interface Parameters {

	/**
	 * Returns the count of parameters.
	 */
	int getCount();

	/**
	 * Returns the given parameter.
	 *
	 * @param i	index of the parameter to return, 0 .. getNumPoints() - 1
	 * @return	parameter or null if i is out of range
	 */
	Parameter getParameter(int i);

	/**
	 * Returns the given parameter.
	 *
	 * @param name	the name of the parameter to return
	 * @return		parameter or null if the named parameter doesn't exist
	 */
	Parameter getParameter(String name);

	/**
	 * Find the index of a parameter by name.
	 *
	 * @param name
	 * @return index of requested parameter or -1 if not found
	 */
	int getParameterIndex(String name);
}