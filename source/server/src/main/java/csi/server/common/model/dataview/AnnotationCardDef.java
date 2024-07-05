package csi.server.common.model.dataview;

import java.util.Date;

import javax.persistence.Entity;

import csi.server.common.model.ModelObject;

@Entity
public class AnnotationCardDef extends ModelObject {
   private String creatorUserName;
   private Date createTime;
   private String content;
   private boolean isEdited;

   public boolean isEdited() {
      return isEdited;
   }

   public void setEdited(boolean edited) {
      isEdited = edited;
   }

   public String getCreatorUserName() {
      return creatorUserName;
   }

   public void setCreatorUserName(String creatorUserName) {
      this.creatorUserName = creatorUserName;
   }

   public Date getCreateTime() {
      return createTime;
   }

   public void setCreateTime(Date createTime) {
      this.createTime = createTime;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }
}
