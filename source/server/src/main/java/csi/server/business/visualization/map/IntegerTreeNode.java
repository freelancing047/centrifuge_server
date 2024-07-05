package csi.server.business.visualization.map;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

public class IntegerTreeNode implements IsSerializable, Serializable {
	private int value;
	private IntegerTreeNode lessNode;
	private IntegerTreeNode moreNode;

	public IntegerTreeNode() {
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public IntegerTreeNode getLessNode() {
		return lessNode;
	}

	public void setLessNode(IntegerTreeNode lessNode) {
		this.lessNode = lessNode;
	}

	public IntegerTreeNode getMoreNode() {
		return moreNode;
	}

	public void setMoreNode(IntegerTreeNode moreNode) {
		this.moreNode = moreNode;
	}

	public void push(int value) {
		if (this.value != value) {
			if (value < this.value) {
				if (lessNode == null) {
					lessNode = new IntegerTreeNode();
					lessNode.setValue(value);
				} else {
					lessNode.push(value);
				}
			} else {
				if (moreNode == null) {
					moreNode = new IntegerTreeNode();
					moreNode.setValue(value);
				} else {
					moreNode.push(value);
				}
			}
		}
	}

	public Set<Integer> getIntegerSet() {
		Set<Integer> integerSet = new HashSet<Integer>();
		integerSet.add(value);
		if (lessNode != null) {
         integerSet.addAll(lessNode.getIntegerSet());
      }
		if (moreNode != null) {
         integerSet.addAll(moreNode.getIntegerSet());
      }
		return integerSet;
	}

	public Object clone() {
		IntegerTreeNode clone = new IntegerTreeNode();
		clone.setValue(getValue());
		if (lessNode != null) {
         clone.setLessNode((IntegerTreeNode)lessNode.clone());
      }
		if (moreNode != null) {
         clone.setLessNode((IntegerTreeNode)moreNode.clone());
      }
		return clone;
	}
}
