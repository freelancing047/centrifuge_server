package csi.shared.core.visualization.graph;

import java.io.Serializable;

public enum GraphLayout implements Serializable {
        circular {
            
            public String getName() {
                return "circular";//NON-NLS
            }
            
            
        },
        centrifuge {

            public String getName() {
                return "centrifuge";//NON-NLS
            }
        },
        forceDirected {

            public String getName() {
                return "forceDirected";//NON-NLS
            }
        },
        treeNodeLink {

            public String getName() {
                return "hierarchical";//NON-NLS
            }
        },
        treeRadial {

            public String getName() {
                return "radial";//NON-NLS
            }
        },
        /**
         * @deprecated Use forceDirected instead.
         */
        @Deprecated
        scramble {
            public String getName() {
                return "scramble";//NON-NLS
            }
        },
        applyForce {

            public String getName() {
                return "applyForce";//NON-NLS
            }
        },
        grid {

            public String getName() {
                return "grid";//NON-NLS
            }
        };

        public String getName(){
            return this.getName();
        }
}
