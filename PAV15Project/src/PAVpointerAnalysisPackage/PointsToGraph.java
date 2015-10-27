package PAVpointerAnalysisPackage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;

public class PointsToGraph {
	private BBVertex[] vertices;
	int size;

	PointsToGraph(SSACFG cfg){
		vertices = new BBVertex[cfg.getMaxNumber()+1];
		for (int i = 0; i < cfg.getMaxNumber()+1; i++) {
			BasicBlock bb = cfg.getBasicBlock(i);
			BBVertex temp = new BBVertex(bb);
			ArrayList<BBEdge> edges = new ArrayList<BBEdge>();
			@SuppressWarnings("rawtypes")
			Iterator succNodes = cfg.getSuccNodes(bb);
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
