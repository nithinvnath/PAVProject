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
	
}
