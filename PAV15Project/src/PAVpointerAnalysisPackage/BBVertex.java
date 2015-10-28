package PAVpointerAnalysisPackage;

import java.util.ArrayList;

import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class BBVertex {
	private BasicBlock bb;
	private ArrayList<BBEdge> edges;
	
	BBVertex(BasicBlock b){
		this.bb = b;
		edges = new ArrayList<BBEdge>();
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
	
}
