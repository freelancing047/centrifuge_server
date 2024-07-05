package csi.server.common.model.visualization.selection;

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MatrixCellSelection implements Selection, Serializable {

    private Set<CellPosition> selectedCells = Sets.newHashSet();

    @Override
    public boolean isCleared() {
        return selectedCells.isEmpty();
    }

    @Override
    public void clearSelection() {
        selectedCells.clear();

    }

    @Override
    public void setFromSelection(Selection selection) {
        this.clearSelection();
        if (selection instanceof MatrixCellSelection) {
            selectedCells.addAll(((MatrixCellSelection) selection).getSelectedCells());
        }

    }

    @Override
    public Selection copy() {
        MatrixCellSelection matrixCellSelection = new MatrixCellSelection();
        matrixCellSelection.setFromSelection(this);
        return matrixCellSelection;
    }

    public boolean contains(int x, int y) {
        CellPosition cp = new CellPosition(x, y);
        return selectedCells.contains(cp);
    }

    public void select(int x, int y) {
        selectedCells.add(new MatrixCellSelection.CellPosition(x, y));
    }

    public void addAll(HashSet<CellPosition> selectedCells) {
        this.selectedCells.addAll(selectedCells);
    }

    public void removeAll(HashSet<CellPosition> selectedCells) {
        this.selectedCells.removeAll(selectedCells);
    }

    public static class CellPosition implements Serializable{
        int x, y;

        public CellPosition() {
            x=0;
            y = 0;
        }

        public CellPosition(int x, int y) {

            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CellPosition) {
                CellPosition that = (CellPosition) obj;
                return this.x == that.x && this.y == that.y;
            }
            return false;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    public HashSet<CellPosition> getSelectedCells() {
        if (!(selectedCells instanceof HashSet)) {
            selectedCells = Sets.newHashSet(selectedCells);
        }
        return (HashSet) selectedCells;
    }
    public void setSelectedCells(HashSet<CellPosition> set){
        selectedCells = set;
    }

}
