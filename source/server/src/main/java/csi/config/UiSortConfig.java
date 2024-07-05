package csi.config;

public class UiSortConfig
    extends AbstractConfigurationSettings
{

    /** Whether to sort fields alphabetically or 'naturally' */
    private boolean sortFieldsAlphabetically = true;

    public boolean isSortFieldsAlphabetically() {
        return sortFieldsAlphabetically;
    }

    public void setSortFieldsAlphabetically(boolean sortFieldsAlphabetically) {
        this.sortFieldsAlphabetically = sortFieldsAlphabetically;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("sortFieldsAlphabetically :" + isSortFieldsAlphabetically() + "\n");
        return sb.toString();
    }

}
