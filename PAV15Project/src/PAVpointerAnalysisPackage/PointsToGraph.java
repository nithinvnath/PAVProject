package PAVpointerAnalysisPackage;

import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class PointsToGraph {
	private BBVertex[] vertices;
	int size;

	PointsToGraph(SSACFG cfg){
		vertices = new BBVertex[cfg.getMaxNumber()];
		for (int i = 0; i < cfg.getMaxNumber(); i++) {
			BasicBlock bb = cfg.getBasicBlock(i);
			BBVertex temp = new BBVertex(bb);
			ArrayList<BBEdge> edges = new ArrayList<BBEdge>();
			@SuppressWarnings("rawtypes")
			Iterator succNodes = cfg.getSuccNodes(bb);
			while (succNodes.hasNext()) {
				edges.add(new BBEdge(bb, (BasicBlock) succNodes.next()));
			}
			temp.setEdges(edges);
			vertices[i]=temp;
		}
	}

	public BBVertex[] getBB() {
		return vertices;
	}

	public void setBB(BBVertex[] bB) {
		vertices = bB;
	}

	public void addVertex(){

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

}
