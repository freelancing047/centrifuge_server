package csi.server.common.model.visualization.table;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.selection.IntPrimitiveSelection;
import csi.server.common.model.visualization.selection.IntegerRowsSelection;

/**
 * Definition of a Table.
 *
 * @author Centrifuge Systems, Inc.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TableViewDef extends VisualizationDef {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TableViewSettings tableViewSettings;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private TableCachedState state;

    @Transient
    @XStreamOmitField
    private IntPrimitiveSelection selection = new IntPrimitiveSelection();

    public TableViewDef() {
        super();
        setType(VisualizationType.TABLE);
    }

    public TableViewDef(String name) {
        this();
        setName(name);
    }

    public TableViewSettings getTableViewSettings() {
        return tableViewSettings;
    }

    public void setTableViewSettings(TableViewSettings tableViewSettings) {
        this.tableViewSettings = tableViewSettings;
    }

    @Override
    public IntPrimitiveSelection getSelection() {
        return selection;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> TableViewDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        TableViewDef myClone = new TableViewDef();

        super.cloneComponents(myClone, fieldMapIn, filterMapIn);

        if (null != getTableViewSettings()) {
            myClone.setTableViewSettings(getTableViewSettings().clone());
        }
        myClone.setState(new TableCachedState());
        return myClone;
    }
    
    @Override
    public <T extends ModelObject, S extends ModelObject> TableViewDef copy(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        TableViewDef myCopy = new TableViewDef();

        super.copyComponents(myCopy, fieldMapIn, filterMapIn);

        if (null != getTableViewSettings()) {
        	myCopy.setTableViewSettings(getTableViewSettings().copy());
        }
        myCopy.setState(new TableCachedState());
        return myCopy;
    }
    
    public TableCachedState getState() {
        return state;
    }

    public void setState(TableCachedState state) {
        this.state = state;
    }

    public void resetCache() {
        setState(new TableCachedState());
    }
}
