package csi.client.gwt.mainapp;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.user.cellview.client.Column;

public abstract class ClickableTextColumn<T> extends Column<T, String> {

  /**
   * Construct a new TextColumn.
   */
  public ClickableTextColumn() {
    super(new ClickableTextCell());
  }
}
