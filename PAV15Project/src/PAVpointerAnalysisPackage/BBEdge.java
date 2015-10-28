package PAVpointerAnalysisPackage;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class BBEdge {
	private BasicBlock start;
	private BasicBlock end;
	private Hashtable<Integer, Set<PointsTo>> table;
	private Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> Columns;
	private boolean isMarked;
	
	public BBEdge(BasicBlock startBlock, BasicBlock endBlock){
		this.start = startBlock;
		this.end = endBlock;
		this.table = new Hashtable<Integer, Set<PointsTo>>();
		this.isMarked = true;
		this.Columns = new Hashtable <Integer, Hashtable<Integer, Set<PointsTo>>>();
	}

	@Override
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("BB"+this.start.getNumber()+"->BB"+this.end.getNumber()+":{");
		Set<Integer> keys = this.table.keySet();
		int i = keys.size();
		for (Integer key : keys) {
			result.append("(v"+key+"-> {");
			Set<PointsTo> p = this.table.get(key);
			for (Iterator<PointsTo> iterator = p.iterator(); iterator.hasNext();) {
				PointsTo pto = (PointsTo) iterator.next();
				result.append(pto.toString());
				if(iterator.hasNext()){
					result.append(", ");
				}
			}
			result.append("})");
			i--;
			if(i>0){
				result.append(", ");
			}
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
		this.table = new Hashtable<Integer, Set<PointsTo>>(table);
	}

	public boolean isMarked() {
		return isMarked;
	}

	public void setMarked(boolean isMarked) {
		this.isMarked = isMarked;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + (isMarked ? 1231 : 1237);
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		return result;
	}
}
