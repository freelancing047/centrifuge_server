package csi.server.common.model.icons;

import java.io.Serializable;
import java.util.List;


public class IconResultDto implements Serializable {

    private List<Icon> results;
    private Integer lastRow;

    public List<Icon> getResults() {
        results.sort((Icon o1, Icon o2) -> o2.getCreateDate().before(o1.getCreateDate()) ? 1 : 0);
        return results;
    }
    public void setResults(List<Icon> results) {
        this.results = results;
    }
    public Integer getLastRow() {
        return lastRow;
    }
    public void setLastRow(Integer lastRow) {
        this.lastRow = lastRow;
    }
}
