package csi.map.controller.model;

public class TypeIdIconUrlPair implements Comparable<TypeIdIconUrlPair> {
    private int typeId;
    private String iconUrl;

    public TypeIdIconUrlPair() {
    }

    public TypeIdIconUrlPair(int typeId, String iconUrl) {
        this.typeId = typeId;
        this.iconUrl = iconUrl;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public int compareTo(TypeIdIconUrlPair o) {
        return Integer.compare(typeId, o.typeId);
    }
}
