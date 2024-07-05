/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.gxt.form;

import java.util.Collection;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.Change;

import csi.shared.core.util.TypedClone;

/**
 * Returns an item from a list store with unapplied changes applied to it without commiting the store.
 * @author Centrifuge Systems, Inc.
 *
 */
public class ListStoreItemCloner {

    public static <M extends TypedClone<M>> M getCloneWithChanges(ListStore<M> store, int itemIndex) {
        M data = store.get(itemIndex);
        M clone = data.getClone();
        Store<M>.Record record = store.getRecord(data);
        Collection<Change<M, ?>> changes = record.getChanges();
        for (Change<M, ?> change : changes) {
            change.modify(clone);
        }

        return clone;
    }

    public static <M extends TypedClone<M>> M getCloneWithChanges(Store<M>.Record record) {
        M data = record.getModel();
        M clone = data.getClone();
        Collection<Change<M, ?>> changes = record.getChanges();
        for (Change<M, ?> change : changes) {
            change.modify(clone);
        }

        return clone;
    }
}
