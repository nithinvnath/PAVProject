/**
 * 
 */
package PAVpointerAnalysisPackage;

import com.ibm.wala.classLoader.IMethod;
//TODO Give a better name
/**
 * @author nithin
 *
 */
public class PointsTo {
	private int lineNumber;
	private IMethod	createdAt;
	private String name;
	private boolean isNull;
	private String field;

	//Constructor
	public PointsTo(int lineNumber, IMethod createadAt) {
		super();
		this.lineNumber = lineNumber;
		this.createdAt = createadAt;
		this.name = "new";
		this.isNull = false;
		this.field="";
	}

	public PointsTo(int lineNumber, String name) {
		super();
		this.lineNumber = lineNumber;
		this.createdAt = null;
		this.name = name;
		this.isNull = false;
		this.field="";
	}
	
	public PointsTo(PointsTo p){
		super();
		this.lineNumber = p.getLineNumber();
		this.createdAt = p.getCreateadAt();
		this.name = p.getName();
		this.isNull = p.getIsNull();
		this.field = p.getField();
	}
	
	public PointsTo(){
		this.isNull=true;
	}

	//Getters and setters
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public IMethod getCreateadAt() {
		return createdAt;
	}
	public void setCreateadAt(IMethod createadAt) {
		this.createdAt = createadAt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsNull() {
		return isNull;
	}

	public void setIsNull(boolean isNull) {
		this.isNull = isNull;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Override
	public String toString() {
		
		if(isNull){
			return "null";
		}else{
			String ret = createdAt.getName().toString()+"."+name+lineNumber;
			if(this.field.equals("")){
				return ret;
			}else{
				return ret+"."+this.field;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + (isNull ? 1231 : 1237);
		result = prime * result + lineNumber;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointsTo other = (PointsTo) obj;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.getName().equals(other.createdAt.getName()))
			return false;
		if (isNull != other.isNull)
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
