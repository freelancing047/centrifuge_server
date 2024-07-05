package csi.server.business.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import csi.server.common.identity.Group;
import csi.server.common.identity.Role;

public class RoleTraverser implements Function<Collection<Role>,List<Role>> {
   public RoleTraverser() {
   }

   @Override
   public List<Role> apply(Collection<Role> roles) {
      List<Role> infos = new ArrayList<Role>();
      Map<String,Boolean> visited = new HashMap<String,Boolean>();
      ArrayDeque<Role> remaining = new ArrayDeque<Role>();

      remaining.addAll(roles);

      while (!remaining.isEmpty()) {
         Role role = remaining.pop();

         if (!visited.containsKey(role.getName())) {
//                RoleInfo roleInfo = GroupActionsService.buildRoleInfo(role);
            infos.add(role);
            visited.put(role.getName(), Boolean.TRUE);

            if (role instanceof Group) {
               remaining.addAll(((Group) role).getMembers());
            }
         }
      }
      return infos;
   }
}
