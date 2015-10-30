package PAVpointerAnalysisPackage;

import java.util.Hashtable;
import java.util.Set;

public class AnalysedMethod {
	private Hashtable<Integer, Set<PointsTo>> inputTable;
	private PointsToGraph graphOutput;
	public Hashtable<Integer, Set<PointsTo>> getInputTable() {
		return inputTable;
	}
	public void setInputTable(Hashtable<Integer, Set<PointsTo>> inputTable) {
		this.inputTable = inputTable;
	}
	public PointsToGraph getGraphOutput() {
		return graphOutput;
	}
	public void setGraphOutput(PointsToGraph graphOutput) {
		this.graphOutput = graphOutput;
	}
	public Hashtable<Integer, Set<PointsTo>> getTable(int BlockNumber){
		assert(BlockNumber < graphOutput.getBB().length);
		BBVertex vertex = graphOutput.getBB()[BlockNumber];
		BBEdge x = vertex.getEdges().get(0);
		return x.getTable();
	}
	
}
