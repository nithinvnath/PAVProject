package PAVpointerAnalysisPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class PointsToGraph {
	private BBVertex[] vertices;
	int size;
	public ArrayList<Integer> returnValIndex;
	public Hashtable<String, Set<PointsTo>> returnTable;

	public PointsToGraph(SSACFG cfg){
		this.size = cfg.getMaxNumber()+1;
		vertices = new BBVertex[cfg.getMaxNumber()+1];
		for (int i = 0; i < cfg.getMaxNumber()+1; i++) {
			BasicBlock bb = cfg.getBasicBlock(i);
			BBVertex temp = new BBVertex(bb);
			ArrayList<BBEdge> edges = new ArrayList<BBEdge>();
			@SuppressWarnings("rawtypes")
			Iterator succNodes = cfg.getSuccNodes(bb);
			cfg.getSuccNodeNumbers(bb);
			List<ISSABasicBlock> exceptionSucc = cfg.getExceptionalSuccessors(bb);
			while (succNodes.hasNext()) {
				BasicBlock next = (BasicBlock) succNodes.next();
				if(!exceptionSucc.contains(next)){
					edges.add(new BBEdge(bb, next));
				}
			}
			temp.setEdges(edges);
			vertices[i]=temp;
		}
		returnValIndex = new ArrayList<Integer>();
	}
	
	public PointsToGraph(PointsToGraph old){
		this.size = old.size;
		vertices = new BBVertex[this.size];
		for(int i=0;i<old.size;++i){
			BBVertex newVertex = new BBVertex(old.getBB()[i]);
			this.vertices[i]=newVertex;
		}
	}

	public BBVertex[] getBB() {
		return vertices;
	}

	public void setBB(BBVertex[] bB) {
		vertices = bB;
	}

	@Override
	public String toString(){
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < vertices.length; i++) {
			BBVertex bb = vertices[i];
			for (BBEdge edge : bb.getEdges()) {
				result.append(edge.toString());
			}
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
		result = prime * result + Arrays.hashCode(vertices);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PointsToGraph))
			return false;
		PointsToGraph other = (PointsToGraph) obj;
		if (size != other.size)
			return false;
		if (!Arrays.equals(vertices, other.vertices))
			return false;
		return true;
	}

}
