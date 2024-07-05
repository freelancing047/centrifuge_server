package csi.client.gwt.widget.ui.uploader.wizards.support;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.server.common.util.DocumentEncoder;

/**
 * Created by centrifuge on 9/11/2015.
 */
public class DataSource implements DocumentEncoder.DataSource {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Int8Array _block;
    int _next = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSource(Int8Array blockIn) {

        _block = blockIn;
    }

    @Override
    public int read(byte[] bufferIn, int startIn, int sizeIn) {

        if (_block.byteLength() > _next) {

            int myLimit = Math.min(_block.byteLength(), (_next + sizeIn));
            int myOffset = 0;

            while (myLimit > _next) {

                bufferIn[myOffset++] = (byte)_block.get(_next++);
            }
            return myOffset;

        } else {

            return 0;
        }
    }
}
