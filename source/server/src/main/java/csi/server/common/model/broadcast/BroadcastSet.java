package csi.server.common.model.broadcast;

import csi.server.business.selection.operations.SelectionOperator;
import csi.server.common.model.visualization.selection.Selection;

public enum BroadcastSet {
    TRUE,
    NOT_BOTH,
    IF_B_THEN_A,
    IF_A_THEN_B,
    A_OR_B,
    NOT_B,
    NOT_A,
    EITHER_A_OR_B_BUT_NOT_BOTH,
    A_IF_AND_ONLY_IF_B,
    A,
    B,
    NEITHER_A_NOR_B,
    A_AND_NOT_B,
    B_AND_NOT_A,
    AND,
    FALSE

}
