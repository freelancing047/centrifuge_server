package csi.server.common.util;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Created by centrifuge on 2/19/2019.
 */
public class Flags implements IsSerializable, Serializable {

    private int _flags = 0;

    private Flags() {

    }

    public Flags(int flagsIn) {

        _flags = flagsIn;
    }

    public int getFlags() {

        return _flags;
    }

    public void setFlags(int flagsIn) {

        _flags = flagsIn;
    }

    public boolean isSet(int flagIn) {

        return (0 != ((1 << flagIn) & _flags));
    }

    public boolean isAnySet(int flagIn) {

        return (0 != (flagIn & _flags));
    }

    public boolean areAllSet(int flagIn) {

        return ((flagIn & _flags) == flagIn);
    }

    public void resetFlag(int flagIn) {

        _flags &= (~(1 << flagIn));
    }

    public void setFlag(int flagIn) {

        _flags |= 1 << flagIn;
    }

    public void clearAll() {

        _flags = 0;
    }

    public void setAll(int flagsIn) {

        _flags |= flagsIn;
    }

    public void setOnly(int flagsIn) {

        _flags = flagsIn;
    }
}
