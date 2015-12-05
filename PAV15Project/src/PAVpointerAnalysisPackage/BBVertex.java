package PAVpointerAnalysisPackage;

import java.util.ArrayList;
import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class BBVertex {
	private BasicBlock bb;
	private ArrayList<BBEdge> edges;
	
	public BBVertex(BasicBlock b){
		this.bb = b;
		this.edges = new ArrayList<BBEdge>();
	}
	
	public BBVertex(BBVertex old) {
		this.bb = old.getBb();
		this.edges = new ArrayList<BBEdge>();
		for(int i=0; i<old.getEdges().size(); ++i ){
			this.edges.add(new BBEdge(old.getEdges().get(0)));
		}
	}
	
	public BasicBlock getBb() {
		return bb;
	}

	public void setBb(BasicBlock bb) {
		this.bb = bb;
	}

	public ArrayList<BBEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<BBEdge> edges) {
		this.edges = edges;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bb == null) ? 0 : bb.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
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
		BBVertex other = (BBVertex) obj;
		if (bb == null) {
			if (other.bb != null)
				return false;
		} else if (!bb.equals(other.bb))
			return false;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		return true;
	}
	
}
