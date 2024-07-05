package csi.client.gwt.widget.display_list_widgets;

import java.util.List;

import csi.server.common.enumerations.SqlToken;
import csi.server.common.model.SqlTokenTreeItem;
import csi.server.common.model.SqlTokenTreeItemList;
import csi.server.common.model.dataview.DataViewDef;

/**
 * Created by centrifuge on 3/21/2015.
 */
public class SqlTokenItemBuilder implements ObjectBuilder<ComponentLabel, SqlToken, SqlTokenTreeItem> {

    @Override
    public SqlTokenTreeItem buildObject(DataViewDef metaDataIn, DisplayListItem<ComponentLabel, SqlToken> displayListItemIn) {

        SqlTokenTreeItem myObject = null;
        SqlToken myToken = displayListItemIn.getObject();

        if (null != myToken) {

            if (myToken.isSystemValue()) {

                myObject = new SqlTokenTreeItem(myToken);

            } else {

                ComponentLabel myLabel = displayListItemIn.getDisplayObject();

                if (null != myLabel) {

                    String myText = myLabel.getText();

                    myObject = new SqlTokenTreeItem(myToken, myToken.removeFormat(metaDataIn, myText));

                } else {

                    myObject = new SqlTokenTreeItem(myToken);
                }

                if (0 < displayListItemIn.getChildCount()) {

                    List<DisplayListItem<ComponentLabel, SqlToken>> myListIn = displayListItemIn.getChildren();
                    SqlTokenTreeItemList myListOut = new SqlTokenTreeItemList();
                    int myOrdinal = 0;

                    for (DisplayListItem<ComponentLabel, SqlToken> myItem : myListIn) {

                        SqlTokenTreeItem myChild = buildObject(metaDataIn, myItem);

                        if (null != myChild) {

                            myChild.setOrdinal(myOrdinal++);
                            myListOut.add(myChild);
                        }
                    }
                    myObject.setArguments(myListOut);
                }
            }
        }

        return myObject;
    }
}
