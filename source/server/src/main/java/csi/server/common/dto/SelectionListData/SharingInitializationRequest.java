package csi.server.common.dto.SelectionListData;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 10/3/2016.
 */
public class SharingInitializationRequest implements IsSerializable {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    List<String> _readList = null;
    List<String> _editList = null;
    List<String> _deleteList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SharingInitializationRequest() {

    }

    public SharingInitializationRequest(List<String> readListIn, List<String> editListIn, List<String> deleteListIn) {

        _readList = readListIn;
        _editList = editListIn;
        _deleteList = deleteListIn;
    }

    public void setReadList(List<String> listIn) {

        _readList = listIn;
    }

    public List<String> getReadList() {

        return _readList;
    }

    public void setEditList(List<String> listIn) {

        _editList = listIn;
    }

    public List<String> getEditList() {

        return _editList;
    }

    public void setDeleteList(List<String> listIn) {

        _deleteList = listIn;
    }

    public List<String> getDeleteList() {

        return _deleteList;
    }
}
