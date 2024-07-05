package csi.shared.gwt.vortex;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CsiPair<T,V> implements IsSerializable{
    
    private T left;
    private V right;
    
    //Think we need this for gwt/vortex
    public CsiPair() {
        
    }
    
    public CsiPair(T left, V right) {
        this.setLeft(left);
        this.setRight(right);
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public V getRight() {
        return right;
    }

    public void setRight(V right) {
        this.right = right;
    }

    
    
}
