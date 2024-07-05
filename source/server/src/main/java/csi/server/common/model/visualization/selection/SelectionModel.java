package csi.server.common.model.visualization.selection;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Map;
import java.util.TreeSet;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SelectionModel extends ModelObject implements Selection {

    public String id;

    @Column(length = 2147483647)
    @Lob
    public TreeSet<Integer> nodes = new TreeSet<Integer>();

    @Column(length = 2147483647)
    @Lob
    public TreeSet<Integer> links = new TreeSet<Integer>();

    @Column(length = 2147483647)
    @Lob
    public TreeSet<String> nodeKeys = new TreeSet<String>();

    @Column(length = 2147483647)
    @Lob
    public TreeSet<String> linkKeys = new TreeSet<String>();

    public SelectionModel() {
        super();
    }

    public void merge(SelectionModel other) {
        nodes.addAll(other.nodes);
        links.addAll(other.links);
    }

    public void reset() {
        nodes.clear();
        links.clear();
    }

    public void resetKeys() {

        nodeKeys = new TreeSet<String>();
        linkKeys = new TreeSet<String>();
    }

    @Override
    public boolean isCleared() {
        return nodes.isEmpty() && links.isEmpty();
    }

    @Override
    public void clearSelection() {
        reset();
    }

    @Override
    public void setFromSelection(Selection selection) {
        if (!(selection instanceof SelectionModel)) {
            clearSelection();
            return;
        }

        SelectionModel selectionModel = (SelectionModel) selection;
        clearSelection();
        merge(selectionModel);
    }


    public <T extends ModelObject, S extends ModelObject> SelectionModel copy(Map<String, S> nodeMapIn) {
        SelectionModel copy = new SelectionModel();
        copy.reset();

        for (Integer i : nodes) {
            copy.nodes.add(i);
        }

        for (Integer i : links) {
            copy.links.add(i);
        }

        return copy;
    }

    @Override
    public Selection copy() {
        return copy(null);
    }
}
