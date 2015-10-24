package PAVpointerAnalysisPackage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class BBEdge {
	private BasicBlock start;
	private BasicBlock end;
	private Hashtable<Integer, Set<PointsTo>> table;
	
	public BBEdge(BasicBlock startBlock, BasicBlock endBlock){
		start = startBlock;
		end = endBlock;
		table = new Hashtable<Integer, Set<PointsTo>>();
	}

	@Override
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("BB"+this.start.getNumber()+"->BB"+this.end.getNumber()+":{");
		Set<Integer> keys = this.table.keySet();
		for (Integer key : keys) {
			result.append("(v"+key+"->{");
			Set<PointsTo> p = this.table.get(key);
			for (Iterator<PointsTo> iterator = p.iterator(); iterator.hasNext();) {
				PointsTo pto = (PointsTo) iterator.next();
				result.append(pto.toString());
				if(iterator.hasNext()){
					result.append(", ");
				}
			}
			result.append("})");
		}
		result.append("}\n");
		return result.toString();
	}

	public BasicBlock getStart() {
		return start;
	}

	public void setStart(BasicBlock start) {
		this.start = start;
	}

	public BasicBlock getEnd() {
		return end;
	}

	public void setEnd(BasicBlock end) {
		this.end = end;
	}

	public Hashtable<Integer, Set<PointsTo>> getTable() {
		return table;
	}

	public void setTable(Hashtable<Integer, Set<PointsTo>> table) {
		this.table = table;
	}
	
}
