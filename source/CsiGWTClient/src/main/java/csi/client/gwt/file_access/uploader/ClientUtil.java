package csi.client.gwt.file_access.uploader;

import com.google.gwt.typedarrays.client.NativeImpl;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * Created by centrifuge on 11/30/2016.
 */
public class ClientUtil {

    public static Int8Array createInt8Array(ArrayBuffer bufferIn) {

        Int8Array myArray = null;

        try {

            myArray = TypedArrays.createInt8Array(bufferIn);

        } catch (Exception myFirstException) {

            myArray = (new NativeImpl()).createInt8Array(bufferIn);
        }
        return myArray;
    }
}
