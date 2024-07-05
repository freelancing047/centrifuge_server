package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.CapcoSection;
import csi.server.common.enumerations.GroupType;


public class GroupDisplay implements IsSerializable {

    private Long _id;
    private GroupType _type;
    private String _name;
    private String _remarks;
    private Boolean external;
    private String _memberGroups;
    private String _parentGroups;
    private CapcoSection _section;
    private String _portionText;

    public void setId(Long idIn) {
        _id = idIn;
    }

    public Long getId() {
        return _id;
    }

    public void setType(GroupType typeIn) {
        _type = typeIn;
    }

    public GroupType getType() {
        return _type;
    }

    public void setRemarks(String remarksIn) {
        _remarks = remarksIn;
    }

    public String getRemarks() {
        return _remarks;
    }

    public Boolean getExternal() {

        return external;
    }

    public void setExternal(Boolean externalIn) {

        external = externalIn;
    }

    public void setName(String nameIn) {
        _name = nameIn;
    }

    public String getName() {
        return _name;
    }

    public void setMemberGroups(String groupsIn) {
        _memberGroups = groupsIn;
    }

    public String getMemberGroups() {
        return _memberGroups;
    }

    public void setParentGroups(String groupsIn) {
        _parentGroups = groupsIn;
    }

    public String getParentGroups() {
        return _parentGroups;
    }

    public void setSection(CapcoSection sectionIn) {
        _section = sectionIn;
    }

    public CapcoSection getSection() {
        return _section;
    }

    public void setPortionText(String portionTextIn) {
        _portionText = portionTextIn;
    }

    public String getPortionText() {
        return _portionText;
    }

    public String getSectionName() {

        return (null != _section) ? _section.name() : null;
    }

    public void loadValues(GroupDisplay groupDataIn) {

        if ((groupDataIn != null) && getId().equals(groupDataIn.getId()) && _name.equalsIgnoreCase(groupDataIn.getName())) {
            setMemberGroups(groupDataIn.getMemberGroups());
        }
    }

    public GroupDisplay copyFrom(GroupDisplay newInfoIn) {

        setId(newInfoIn.getId());
        setType(newInfoIn.getType());
        setName(newInfoIn.getName());
        setRemarks(newInfoIn.getRemarks());
        setMemberGroups(newInfoIn.getMemberGroups());
        setParentGroups(newInfoIn.getParentGroups());

        return  this;
    }
}
