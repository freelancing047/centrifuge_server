package csi.server.common.model.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;
import csi.server.common.dto.FieldListAccess;
import csi.server.common.model.ModelObject;
import csi.server.common.model.OrderedField;
import csi.server.common.util.Update;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ConcatFunction extends ScriptFunction implements IsSerializable {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    protected List<OrderedField> fields;
    protected String separator;

    public ConcatFunction() {
        super();
    }

    public List<OrderedField> getFields() {
        if (fields == null) {
            fields = new ArrayList<OrderedField>();
        }

        return fields;
    }

    public void setFields(List<OrderedField> fields) {
        this.fields = fields;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

   @Override
   @GwtIncompatible("FieldSorter.getSortedFields")
   public String generateScript(FieldListAccess modelIn) {
      StringBuilder script = new StringBuilder();
      String sepStr = getSeparator();
      int i = 0;

      script.append("var values = new Array();\n");

      for (OrderedField f : FieldSorter.getSortedFields(getFields())) {
         script.append("values[" + i + "] = csiRow.getString('" + f.getFieldDef(modelIn).getFieldName() + "');\n");
         i++;
      }
      if (sepStr == null) {
         sepStr = "";
      } else if (sepStr.equals("&#32;")) {
         sepStr = " ";
      } else if (sepStr.equals("&#09;")) {
         sepStr = "\t";
      }
      return script.append("var csiResult = values.join('").append(sepStr).append("');\n").toString();
   }

    @Override
    public <T extends ModelObject> ConcatFunction clone(Map<String, T> fieldMapIn){
        if (fieldMapIn == null) {
            fieldMapIn = new HashMap<String,T>();
        }
        {
            Object copyOfThis = fieldMapIn.get(this.getUuid());
            if (copyOfThis != null) {
                return (ConcatFunction) copyOfThis;
            }
        }
        ConcatFunction copy = new ConcatFunction();
        fieldMapIn.put(getUuid(), (T) copy);
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        super.copyComponents(copy);
        copy.separator = this.separator;
        copy.fields = new ArrayList<OrderedField>();
        if(fields != null) {
            for (OrderedField o : fields) {
                copy.fields.add(o.clone(fieldMapIn));
            }
        }
        return copy;
    }

    @Override
    public ConcatFunction copy(Map<String, Object> copies) {
        if (copies == null) {
            copies = new HashMap<String,Object>();
        }
        {
            Object copyOfThis = copies.get(this.getUuid());
            if (copyOfThis != null) {
                return (ConcatFunction) copyOfThis;
            }
        }
        ConcatFunction copy = new ConcatFunction();
        copies.put(getUuid(), copy);
        copy.clientProperties = new CsiMap<String, String>();
        copy.clientProperties.putAll(this.getClientProperties());
        copy.name = this.name;
        copy.ordinal = this.ordinal;
        copy.uuid = this.uuid;

        copy.separator = this.separator;
        copy.fields = new ArrayList<OrderedField>();
        if(fields != null) {
            for (OrderedField o : fields) {
                copy.fields.add(o.copy(copies));
            }
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConcatFunction fullClone() {

        ConcatFunction myClone = new ConcatFunction();

        fullCloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConcatFunction clone() {

        ConcatFunction myClone = new ConcatFunction();

        cloneComponents(myClone);

        return myClone;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cloneComponents(ModelObject cloneIn) {

        super.cloneComponents(cloneIn);

        cloneContents((ConcatFunction) cloneIn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fullCloneComponents(ModelObject cloneIn) {

        super.fullCloneComponents(cloneIn);

        cloneContents((ConcatFunction) cloneIn);
    }

    @Override
    public void updateInPlace(ScriptFunction sourceIn) {

        super.updateInPlace(sourceIn);

        setSeparator(((ConcatFunction) sourceIn).getSeparator());
        setFields(((ConcatFunction)sourceIn).cloneFields());

        updateFieldList(((ConcatFunction) sourceIn).getFields());
    }

    private void cloneContents(ConcatFunction cloneIn) {

        cloneIn.setSeparator(getSeparator());
        cloneIn.setFields(cloneFields());
    }

    private List<OrderedField> cloneFields() {

        if (getFields() != null) {

            List<OrderedField>  myList = new ArrayList<OrderedField>();

            for (OrderedField myItem : getFields()) {

                myList.add(myItem.clone());
            }

            return myList;

        } else {

            return null;
        }
    }

    private void updateFieldList(List<OrderedField> newListIn) {

        fields = Update.updateListInPlace(fields, newListIn);
    }
}
