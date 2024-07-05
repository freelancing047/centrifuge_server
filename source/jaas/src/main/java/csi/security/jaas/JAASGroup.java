package csi.security.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class JAASGroup extends JAASRole implements Group {

    private String name;
    private Set<Principal> members;

    public JAASGroup(String name) {
        super(name);
        members = new HashSet<Principal>();
    }

    @Override
    public boolean isMember(Principal member) {
        return members.contains(member);
    }

    @Override
    public boolean addMember(Principal member) {
        synchronized (members) {
            return members.add(member);
        }
    }

    @Override
    public Enumeration<? extends Principal> members() {
        return Collections.enumeration(members);
    }

    @Override
    public boolean removeMember(Principal member) {
        synchronized (members) {
            return members.remove(member);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JAASGroup)) {
            return false;
        }

        JAASGroup that = (JAASGroup) obj;

        return getName().equals(that.getName());
    }

}
