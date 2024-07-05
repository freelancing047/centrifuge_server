package csi.server.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.BundleDef;
import csi.server.common.model.visualization.graph.BundleOp;
import csi.server.common.model.visualization.graph.DirectionDef;
import csi.server.common.model.visualization.graph.GraphPlayerSettings;
import csi.server.common.model.visualization.graph.LinkDef;
import csi.server.common.model.visualization.graph.NodeDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class FieldReferenceAugmentor {

    private boolean hasChanges = false;
    private DataViewDef def;
    private FieldListAccess _model;
    private Collection<String> msgs;

    public FieldReferenceAugmentor(File serverdir, DataViewDef def) {
        this.def = def;
        _model = def.getModelDef().getFieldListAccess();
        msgs = new ArrayList<String>();
    }

    public boolean augment() {

        DataModelDef mvModel = def.getModelDef();
        fixVisualizations(mvModel);
        return hasChanges;
    }

    private void fixVisualizations(DataModelDef modelIn) {
        List<VisualizationDef> visualizations = modelIn.getVisualizations();
        if (visualizations == null) {
            return;
        }

        for (VisualizationDef vd : visualizations) {
            fix(vd);
        }
    }

    private void fix(VisualizationDef vd) {
        if (vd == null) {
            return;
        }
        if (vd instanceof RelGraphViewDef) {
            fixRG((RelGraphViewDef) vd);
        }
    }

    private void createStaticField(FieldDef field) {
        field.setFieldType(FieldType.STATIC);
        field.setStaticText("Invalid");
        _model.addFieldDef(field);
        String msg = String.format("Field reference to %1$s converted to a static field.", field.getFieldName());
        msgs.add(msg);
        hasChanges = true;

    }

    private boolean isValid(FieldDef field) {
        if (field == null) {
            return false;
        }

        if ((field.getFieldType() != FieldType.COLUMN_REF)
                && (field.getFieldType() != FieldType.LINKUP_REF)
                && (field.getFieldType() != FieldType.DERIVED)
                && (field.getFieldType() != FieldType.SCRIPTED) ) {
            return true;
        }

        Map<String, FieldDef> fieldMap = _model.getFieldMapByUuid();
        boolean exists = fieldMap.containsKey(field.getUuid());
        return exists;
    }

    private void fix(NodeDef node) {
        if (node == null) {
         return;
      }

        Set<AttributeDef> attrs = node.getAttributeDefs();
        if (attrs != null) {
            for (AttributeDef a : attrs) {
                fix(a);
            }
        }
    }

    private void fix(AttributeDef attr) {
        if (attr == null) {
         return;
      }

        FieldDef field = attr.getFieldDef();
        if (isValid(field)) {
            if (attr.getName().equals(ObjectAttributes.CSI_INTERNAL_ICON)) {
                String s = field.getStaticText();
                field.setStaticText(fixIconUrl(s));

            }
            return;
        }

        // CTWO-6761 -- handle null fields. this accounts for 'built-in' graph attributes
        // e.g. degree of a node.
        if ((field == null) && (attr.getReferenceName() != null)) {
            return;
        }

        String name = (field != null) ? field.mapKey() : null;
        FieldDef ref = null;
        if (name != null) {
            ref = _model.getFieldDefByName(name);
        }

        if (ref != null) {
            String msg = String.format("Attribute definition fixed for %1$s", attr.getName());
            msgs.add(msg);
            hasChanges = true;
            attr.setFieldDef(ref);
        } else {
            createStaticField(field);
        }
    }

    private String fixIconUrl(String iconUrl) {
//        try {
//            File f = new File(serverdir, "webapps" + iconUrl.replace('\\', '/'));
//            if (!f.exists()) {
//                String prefix = "/Centrifuge/resources/icons/";
//                File f2 = new File(serverdir, "webapps"
//                        + (prefix + "Baseline/" + iconUrl.substring(prefix.length())).replace('\\', '/'));
//                if (f2.exists()) {
//                    return prefix + "Baseline/" + iconUrl.substring(prefix.length());
//                }
//            }
//        } catch (Exception e) {
//
//        }
        return iconUrl;
    }

    private void fixRG(RelGraphViewDef def) {
        if (def == null) {
         return;
      }

        List<BundleDef> bundleDefs = def.getBundleDefs();
        List<LinkDef> linkDefs = def.getLinkDefs();
        List<NodeDef> nodeDefs = def.getNodeDefs();
        GraphPlayerSettings playerSettings = def.getPlayerSettings();

        Map<String, NodeDef> nodesByName = new HashMap<String, NodeDef>();
        for (NodeDef n : nodeDefs) {
            String name = n.getName();
            if (name != null) {
                nodesByName.put(name, n);
            }
        }

        if (bundleDefs != null) {
            for (BundleDef b : bundleDefs) {
                fix(nodeDefs, nodesByName, b);
            }
        }

        if (linkDefs != null) {
            for (LinkDef l : linkDefs) {
                fix(l);
            }
        }

        if (nodeDefs != null) {

            for (NodeDef n : nodeDefs) {
                fix(n);
            }
        }

        fix(playerSettings);

    }

   private void fix(GraphPlayerSettings playerSettings) {
      if (playerSettings != null) {
         FieldDef field;

         if (playerSettings.startField != null) {
            field = playerSettings.startField;

            if (!isValid(field)) {
               FieldDef ref = (field == null) ? null : _model.getFieldDefByName(field.mapKey());

               if (ref == null) {
                  createStaticField(field);
               } else {
                  playerSettings.startField = ref;
                  String msg = String.format("Updated player settings start field");

                  msgs.add(msg);

                  hasChanges = true;
               }
            }
         }
         if (playerSettings.endField != null) {
            field = playerSettings.endField;

            if (!isValid(field)) {
               FieldDef ref = (field == null) ? null : _model.getFieldDefByName(field.mapKey());

               if (ref == null) {
                  createStaticField(field);
               } else {
                  playerSettings.endField = ref;
                  String msg = String.format("Updated player settings end field");

                  msgs.add(msg);
                  hasChanges = true;
               }
            }
         }
      }
   }

    private void fix(LinkDef l) {
        if (l == null) {
         return;
      }

        Set<AttributeDef> attrs = l.getAttributeDefs();
        DirectionDef direction = l.getDirectionDef();
        NodeDef s = l.getNodeDef1();
        NodeDef t = l.getNodeDef2();

        if (attrs != null) {
            for (AttributeDef attr : attrs) {
                fix(attr);
            }
        }

        fix(direction);
        fix(s);
        fix(t);

    }

   private void fix(DirectionDef direction) {
      if (direction != null) {
         FieldDef field = direction.getFieldDef();

         if (!isValid(field)) {
            FieldDef ref = (field == null) ? null : _model.getFieldDefByName(field.mapKey());

            if (ref == null) {
               createStaticField(field);
            } else {
               direction.setFieldDef(ref);

               String msg =
                  String.format("Direction fixed for field %1$s", (field == null) ? "null" : field.getFieldName());

               msgs.add(msg);
               hasChanges = true;
            }
         }
      }
   }

   private void fix(List<NodeDef> nodes, Map<String, NodeDef> nodesByName, BundleDef b) {
      if (b != null) {
         List<BundleOp> operations = new ArrayList<BundleOp>(b.getOperations());

         for (BundleOp op : operations) {
            fix(op);

            NodeDef node = op.getNodeDef();

            if (!nodes.contains(node)) {
               // stale reference to the node, fix this up!
               NodeDef ref = nodesByName.get(node.getName());
               String msg = null;

               if (ref == null) {
                  b.getOperations().remove(op);

                  msg = String.format("Removed invalid bundle operation for non-existent node %1$s", node.getName());
               } else {
                  op.setNodeDef(ref);

                  msg = String.format("Updated bundle operation to reference correct node %1$s", node.getName());
               }
               msgs.add(msg);
               hasChanges = true;
            }
         }
      }
   }

   private void fix(BundleOp op) {
      if (op != null) {
        FieldDef field = op.getField();

        if (!isValid(field)) {
           FieldDef ref = (field == null) ? null : _model.getFieldDefByName(field.mapKey());

           if (ref == null) {
              createStaticField(field);
           } else {
              op.setField(ref);

              String msg =
                 String.format("Bundle Operation fixed for field %1$s", (field == null) ? "null" : field.getFieldName());

              msgs.add(msg);

              hasChanges = true;
           }
        }
      }
   }

   public Collection<String> getMessages() {
      return msgs;
   }
}
