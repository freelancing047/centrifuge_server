package csi.server.common.dto.SelectionListData;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 10/3/2016.
 */
public class SharingRequest implements IsSerializable {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    List<List<String>> _readList = null;
    List<List<String>> _editList = null;
    List<List<String>> _deleteList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SharingRequest() {

    }

    public SharingRequest(List<List<String>> readListIn, List<List<String>> editListIn, List<List<String>> deleteListIn) {

        _readList = readListIn;
        _editList = editListIn;
        _deleteList = deleteListIn;
    }

    public void setReadList(List<List<String>> listIn) {

        _readList = listIn;
    }

    public List<List<String>> getReadList() {

        return _readList;
    }

    public void setEditList(List<List<String>> listIn) {

        _editList = listIn;
    }

    public List<List<String>> getEditList() {

        return _editList;
    }

    public void setDeleteList(List<List<String>> listIn) {

        _deleteList = listIn;
    }

    public List<List<String>> getDeleteList() {

        return _deleteList;
    }
}
