package csi.map.controller.payloadbuilder;

import csi.map.controller.model.Payload;

public class DeferToNewCachePayloadBuilder extends AbstractPayloadBuilder {
   public DeferToNewCachePayloadBuilder() {
      super(null, null);
   }

   @Override
   public void build() {
   }

   @Override
   protected void decorateWithMapCache() {
   }

   @Override
   protected void decorateWithOthers() {
   }

   @Override
   protected void decorateWithMapSettings() {
   }

   @Override
   public Payload getPayload() {
      payload = new Payload();
      payload.setDeferToNewCache();
      return payload;
   }
}
