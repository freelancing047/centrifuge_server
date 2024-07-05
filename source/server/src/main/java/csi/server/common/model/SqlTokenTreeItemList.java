package csi.server.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.interfaces.ParameterListAccess;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.Update;

/**
 * Created by centrifuge on 3/24/2015.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SqlTokenTreeItemList extends ModelObject implements InPlaceUpdate<SqlTokenTreeItemList> {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    private List<SqlTokenTreeItem> tokenList;
    private int ordinal;

    public SqlTokenTreeItemList() {

        tokenList = new ArrayList<SqlTokenTreeItem>();
        ordinal = 0;
    }

    public SqlTokenTreeItemList(SqlTokenTreeItem itemIn) {

        this();
        add(itemIn);
    }

    public void setTokenList(List<SqlTokenTreeItem> tokenListIn) {

        tokenList = tokenListIn;
    }

    public List<SqlTokenTreeItem> getTokenList() {

        return tokenList;
    }

    public void setOrdinal(int ordinalIn) {

        ordinal = ordinalIn;
    }

    public int getOrdinal() {

        return ordinal;
    }

    public SqlTokenTreeItem get(int indexIn) {

        return tokenList.get(indexIn);
    }

    public int size() {

        return tokenList.size();
    }

    public void add(SqlTokenTreeItem itemIn) {

        itemIn.setOrdinal(ordinal++);
        tokenList.add(itemIn);
    }

    public void set(int indexIn, SqlTokenTreeItem itemIn) {

        itemIn.setOrdinal(indexIn);
        tokenList.set(indexIn, itemIn);
    }

    public Map<String, FieldDef> mapRequiredFields(Map<String, FieldDef> mapIn, FieldListAccess modelIn) {

        Map<String, FieldDef>myMap = (null != mapIn) ? mapIn : new HashMap<String, FieldDef>();

        for (SqlTokenTreeItem myItem : tokenList) {

            myItem.mapRequiredFields(myMap, modelIn);
        }
        return myMap;
    }

    public void incrementRequiredParameters(Map<String, QueryParameterDef> mapIn) {

        for (SqlTokenTreeItem myItem : tokenList) {

            myItem.incrementRequiredParameters(mapIn);
        }
    }

    public void decrementRequiredParameters(Map<String, QueryParameterDef> mapIn) {

        for (SqlTokenTreeItem myItem : tokenList) {

            myItem.decrementRequiredParameters(mapIn);
        }
    }

    public Map<String, QueryParameterDef> mapRequiredParameters(Map<String, QueryParameterDef> mapIn, ParameterListAccess metaIn) {

        Map<String, QueryParameterDef>myMap = (null != mapIn) ? mapIn : new HashMap<String, QueryParameterDef>();

        for (SqlTokenTreeItem myItem :tokenList) {

            myItem.mapRequiredParameters(myMap, metaIn);
        }
        return myMap;
    }

    public SqlTokenTreeItemList fullClone() {

        SqlTokenTreeItemList myList = null;

        if (null != tokenList) {

            myList = new SqlTokenTreeItemList();

            for (SqlTokenTreeItem myItem : tokenList) {

                myList.add(myItem.fullClone());
            }
        }
        return myList;
    }

    public SqlTokenTreeItemList clone() {

        SqlTokenTreeItemList myList = null;

        if (null != tokenList) {

            myList = new SqlTokenTreeItemList();

            for (SqlTokenTreeItem myItem : tokenList) {

                myList.add(myItem.clone());
            }
        }
        return myList;
    }

    @Override
    public void updateInPlace(SqlTokenTreeItemList sourceIn) {

        setOrdinal(sourceIn.getOrdinal());

        tokenList = Update.updateListInPlace(tokenList, sourceIn.getTokenList());
    }
}
