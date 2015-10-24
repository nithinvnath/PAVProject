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


	//Constructor
	public PointsTo(int lineNumber, IMethod createadAt) {
		super();
		this.lineNumber = lineNumber;
		this.createdAt = createadAt;
		this.name = "new";
		this.isNull = false;
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

	@Override
	public String toString() {
		if(isNull){
			return "null";
		}else{
			return name+lineNumber;
		}
	}


}