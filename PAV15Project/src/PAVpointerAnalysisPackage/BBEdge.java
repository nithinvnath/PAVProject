package PAVpointerAnalysisPackage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class BBEdge {
	private BasicBlock start;
	private BasicBlock end;
	private Hashtable<String, Set<PointsTo>> table;
	private Hashtable<Integer, Hashtable<String, Set<PointsTo>>> columns;
	private boolean isMarked;

	public BBEdge(BasicBlock startBlock, BasicBlock endBlock){
		this.start = startBlock;
		this.end = endBlock;
		this.table = new Hashtable<String, Set<PointsTo>>();
		this.isMarked = true;
		this.columns = new Hashtable <Integer, Hashtable<String, Set<PointsTo>>>();
	}
	
	private Hashtable<String, Set<PointsTo>> copy( Hashtable<String, Set<PointsTo>> source){
		Hashtable<String, Set<PointsTo>> result = new  Hashtable<String, Set<PointsTo>>();
		Enumeration<String> keys = source.keys();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			Set<PointsTo> ptoSet = source.get(key);
			Set<PointsTo> newPtoSet = new HashSet<PointsTo>();
			for(PointsTo pto : ptoSet){
				PointsTo newpto = new PointsTo(pto);
				newPtoSet.add(newpto);
			}
			result.put(key, newPtoSet);
		}
		return result;
	}

	public BBEdge(BBEdge old){
		this.start = old.getStart();
		this.end = old.getEnd();
		this.table = copy(old.getTable());
		this.columns = old.getColumns();
	}
	
	@Override
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("BB"+this.start.getNumber()+"->BB"+this.end.getNumber()+":\n");
		Set<String> keys = this.table.keySet();
		int i = keys.size();

		result.append("{");
		for (String key : keys) {
			String tkey;
			if(key.length()<=2){
				tkey ="v"+key;
			}else{
				tkey = key;
			}
			result.append("("+tkey+" -> {");
			Set<PointsTo> p = this.table.get(key);
			for (Iterator<PointsTo> iterator = p.iterator(); iterator.hasNext();) {
				PointsTo pto = (PointsTo) iterator.next();
				result.append(pto.toString());
				if(iterator.hasNext()){
					result.append(",\n ");
				}
			}
			result.append("})");
			i--;
			if(i>0){
				result.append(",\n ");
			}
		}
		result.append("}\n\n");
		return result.toString();
	}

	public String printColumns(HashMap<String, ArrayList<AnalysedMethod>> analysedMethods, HashMap<Integer, Hashtable<String, Set<PointsTo>>> tableLabels){
		StringBuffer result = new StringBuffer();
		result.append("BB"+this.start.getNumber()+"->BB"+this.end.getNumber()+": \n");
		int numColumns = this.getColumns().size();
		for(int i=0;i<numColumns;++i){
			Hashtable<Integer, Hashtable<String, Set<PointsTo>>> columnSet = this.getColumns();
			Set<Integer> tableKeys = tableLabels.keySet();
			for(Integer tableKey : tableKeys){
				if(columnSet.containsKey(tableKey)){
					result.append("C"+tableKey+":\n");
					result.append(columnSet.get(tableKey).toString()+"\n");
				}
			}
		}
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

	public Hashtable<String, Set<PointsTo>> getTable() {
		return table;
	}

	public void setTable(Hashtable<String, Set<PointsTo>> table) {
		this.table = new Hashtable<String, Set<PointsTo>>(table);
	}

	public boolean isMarked() {
		return isMarked;
	}

	public void setMarked(boolean isMarked) {
		this.isMarked = isMarked;
	}

	public Hashtable<Integer, Hashtable<String, Set<PointsTo>>> getColumns() {
		return columns;
	}

	public void setColumns(Hashtable<Integer, Hashtable<String, Set<PointsTo>>> columns) {
		this.columns = columns;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BBEdge other = (BBEdge) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (isMarked != other.isMarked)
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		return true;
	}

}
