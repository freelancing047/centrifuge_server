package csi.server.common.util;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.enumerations.DisplayMode;

/**
 * Created by centrifuge on 1/25/2016.
 */
public class DisplayableObject implements IsSerializable {
   private DisplayMode _displayMode = DisplayMode.NORMAL;
   private DisplayMode _priorMode = DisplayMode.NORMAL;
   private int _ordinal = 0;

   public DisplayableObject() {
   }

   public DisplayableObject(DisplayMode displayModeIn) {
      _displayMode = displayModeIn;
   }

   public void setPriorMode(DisplayMode priorModeIn) {
   _priorMode = priorModeIn;
   }

   public DisplayableObject(DisplayMode displayModeIn, int ordinalIn) {
      _displayMode = displayModeIn;
      _ordinal = ordinalIn;
   }

   public void setPriorMode(DisplayMode priorModeIn, int ordinalIn) {
      _priorMode = priorModeIn;
      _ordinal = ordinalIn;
   }

   public DisplayMode getPriorMode() {
      return _priorMode;
   }

   public void setDisplayMode(DisplayMode displayModeIn) {
      _displayMode = displayModeIn;
   }

   public DisplayMode getDisplayMode() {
      return _displayMode;
   }

   public int getOrdinal() {
      return _ordinal;
   }

   public void setOrdinal(int ordinalIn) {
      _ordinal = ordinalIn;
   }

   public boolean isSpecial() {
      return (DisplayMode.SPECIAL == _displayMode);
   }

   public boolean isError() {
      return (DisplayMode.ERROR == _displayMode);
   }

   public boolean isDisabled() {
      return (DisplayMode.DISABLED == _displayMode);
   }

   public boolean isComponent() {
      return (DisplayMode.COMPONENT == _displayMode);
   }

   public void clearComponent() {
      if (DisplayMode.COMPONENT == _displayMode) {
         _displayMode = _priorMode;
         _priorMode = DisplayMode.COMPONENT;
      }
   }

   public void setComponent() {
      if (DisplayMode.COMPONENT != _displayMode) {
         _priorMode = _displayMode;
         _displayMode = DisplayMode.COMPONENT;
      }
   }

   public void clearSpecial() {
      if (DisplayMode.SPECIAL == _displayMode) {
         _displayMode = _priorMode;
         _priorMode = DisplayMode.SPECIAL;
      }
   }

   public void setSpecial() {
      if (DisplayMode.SPECIAL != _displayMode) {
         _priorMode = _displayMode;
         _displayMode = DisplayMode.SPECIAL;
      }
   }

   public void clearError() {
      if (DisplayMode.ERROR == _displayMode) {
         _displayMode = _priorMode;
         _priorMode = DisplayMode.ERROR;
      }
   }

   public void setError() {
      if (DisplayMode.ERROR != _displayMode) {
         _priorMode = _displayMode;
         _displayMode = DisplayMode.ERROR;
      }
   }

   public void enable() {
      if (DisplayMode.DISABLED == _displayMode) {
         _displayMode = _priorMode;
         _priorMode = DisplayMode.DISABLED;
      }
   }

   public void disable() {
      if (DisplayMode.DISABLED != _displayMode) {
         _priorMode = _displayMode;
         _displayMode = DisplayMode.DISABLED;
      }
   }
}
