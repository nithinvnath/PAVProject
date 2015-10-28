package PAVpointerAnalysisPackage;

// Do NOT import the pointer analysis package
import java.io.*;
import java.util.*;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;

public class SetUpAnalysis {

	private String classpath;
	private String mainClass;
	private String analysisClass;
	private String analysisMethod;

	//START: NO CHANGE REGION
	private AnalysisScope scope;	// scope defines the set of files to be analyzed
	private ClassHierarchy ch;		// Generate the class hierarchy for the scope
	private Iterable<Entrypoint> entrypoints;	// In the call graph, these are the entrypoint nodes
	private AnalysisOptions options;	// Specify options for the call graph builder
	private CallGraphBuilder builder;	// Builder object for the call graph
	private CallGraph cg;			// Call graph for the program

	public SetUpAnalysis(String classpath, String mainClass, String analysisClass, String analysisMethod)
	{
		this.classpath = classpath;
		this.mainClass = mainClass;
		this.analysisClass = analysisClass;
		this.analysisMethod = analysisMethod;


	}

	/**
	 * Defines the scope of analysis: identifies the classes for analysis
	 * @throws Exception
	 */
	public void buildScope() throws Exception
	{
		FileProvider f = new FileProvider();
		scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(classpath, f.getFile(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
	}

	/**
	 * Builds the hierarchy among the classes to be analyzed: if B extends A, then A is a superclass of B, etc
	 * @throws Exception
	 */
	public void buildClassHierarchy() throws Exception
	{
		ch = ClassHierarchy.make(scope);
	}

	/**
	 * The nodes of a call graph are methods. This method defines the "entry points" in the call graph.
	 * Note: The entry point may not necessarily be the main method.  
	 */
	public void buildEntryPoints()
	{
		entrypoints = Util.makeMainEntrypoints(scope, ch, mainClass);
	}

	/**
	 * Options to build the required call graph.
	 */
	public void setUpCallGraphConstruction()
	{
		options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints);
		builder = Util.makeZeroCFABuilder(options, new AnalysisCache(), ch, scope);
	}

	/**
	 * Build the call graph.
	 * @throws Exception
	 */
	public void generateCallGraph() throws Exception
	{
		cg = builder.makeCallGraph(options, null);
	}
	//END: NO CHANGE REGION

	/**
	 * These are methods for testing purposes. You can call to these to check whether Wala is setup properly.
	 * This method prints the nodes of the call graph.
	 */
	public void printNodes()
	{
		System.out.println("Displaying Application's Call Graph nodes: ");
		Iterator<CGNode> nodes = cg.iterator();

		// Printout the nodes in the call-graph
		while(nodes.hasNext()) {
			String nodeInfo = nodes.next().toString();
			if(nodeInfo.contains("Application"))
				System.out.println(nodeInfo);
		}
	}

	/**
	 * This method prints the IR of the analysisMethod
	 */
	public void printIR() {
		System.out.println("\n\n");
		Iterator<CGNode> nodes = cg.iterator();
		CGNode target = null;
		while(nodes.hasNext()) {
			CGNode node = nodes.next();
			String nodeInfo = node.toString();
			if(nodeInfo.contains(analysisClass) && nodeInfo.contains(analysisMethod)) {
				target = node;
				break;
			}
		}
		if(target!=null) {
			System.out.println("The IR of method " + target.getMethod().getSignature() + " is:");
			System.out.println(target.getIR().toString());
		} else {
			System.out.println("The given method in the given class could not be found");
		}
	}

	/**
	 * Here, you need to fill in code to complete the rest of the analysis workflow.
	 * see project presentation and the write-up for details
	 */
	public PointsToGraph setupGraph(String methodName){
		Iterator<CGNode> nodes = cg.iterator();
		PointsToGraph result;
		CGNode target = null;
		while(nodes.hasNext()) {
			CGNode node = nodes.next();
			String nodeInfo = node.toString();
			if(nodeInfo.contains(analysisClass) && nodeInfo.contains(methodName)) {
				target = node;
				break;
			}
		}
		if(target!=null) {
			result = new PointsToGraph(target.getIR().getControlFlowGraph());
		} else {
			result = null;
			System.out.println("The given method in the given class could not be found");
		}
		return result;
	}

	public CGNode getCGNode(String methodName){
		Iterator<CGNode> nodes = cg.iterator();
		CGNode target = null;
		while(nodes.hasNext()) {
			CGNode node = nodes.next();
			String nodeInfo = node.toString();
			if(nodeInfo.contains(analysisClass) && nodeInfo.contains(methodName)) {
				target = node;
				break;
			}
		}
		return target;
	}

	/* Copies the content of hash table to a new table */
	private Hashtable<Integer, Set<PointsTo>> copy( Hashtable<Integer, Set<PointsTo>> source){
		Hashtable<Integer, Set<PointsTo>> result = new  Hashtable<Integer, Set<PointsTo>>();
		Enumeration<Integer> keys = source.keys();
		while(keys.hasMoreElements()){
			Integer key = keys.nextElement();
			Set<PointsTo> ptoSet = source.get(key);
			Set<PointsTo> newPtoSet = new HashSet<PointsTo>(ptoSet);
			result.put(key, newPtoSet);
		}
		return result;
	}

	private boolean isDifferent(Hashtable<Integer, Set<PointsTo>> oldTable, Hashtable<Integer, Set<PointsTo>> newTable){
		Enumeration<Integer> oldKeys = oldTable.keys();
		Enumeration<Integer> newKeys = newTable.keys();
		while(newKeys.hasMoreElements() && oldKeys.hasMoreElements()){
			Integer newKey = newKeys.nextElement();
			oldKeys.nextElement();
			if(!oldTable.containsKey(newKey)){
				return true;
			}
			if(!oldTable.get(newKey).equals(newTable.get(newKey))){
				return true;
			}
		}
		if(newKeys.hasMoreElements() || oldKeys.hasMoreElements()){
			return true;
		}
		return false;
	}

	public Hashtable<Integer, Set<PointsTo>> analyseMethod(Hashtable<Integer, Set<PointsTo>> inputTable, String methodName){
		//Add all edges to a queue
		PointsToGraph graph = setupGraph(methodName);
		IR ir = getCGNode(methodName).getIR();
		Queue<BBEdge> queue = new LinkedList<BBEdge>();
		for (BBVertex vertex : graph.getBB()) {
			queue.addAll(vertex.getEdges());
		}
		//Iterate while queue is not empty
		while(!queue.isEmpty()){
			BBEdge edge = queue.remove();
			//Get the current table of the edge
			Hashtable<Integer, Set<PointsTo>> table = edge.getTable();
			BBVertex endVertex = graph.getBB()[edge.getEnd().getNumber()];

			if(edge.getStart().isEntryBlock()){
				edge.setTable(inputTable);
				edge.setMarked(false);
			}

			boolean isIdentityTransfer = true;

			List<SSAInstruction> instructions = edge.getEnd().getAllInstructions();
			for (SSAInstruction instruction : instructions) {

				/* Transfer function for "new" statements */
				if(instruction instanceof SSANewInstruction){
					isIdentityTransfer = false;
					//Applying transfer function to the existing table to make newTable
					Hashtable<Integer, Set<PointsTo>> newTable = copy(table);
					newTable.remove(instruction.getDef());
					Set<PointsTo> newSet = (Set<PointsTo>) new HashSet<PointsTo>();
					newSet.add(new PointsTo(instruction.iindex, edge.getStart().getMethod()));
					newTable.put(instruction.getDef(), newSet);

					//Assigning the new table to successor
					for (BBEdge bbedge : endVertex.getEdges()) {
						bbedge.setTable(newTable);
						if(!bbedge.isMarked()){
							bbedge.setMarked(true);
							queue.add(bbedge);
						}
					}
					//System.out.println(instruction.iindex);
					//System.out.println(instruction.toString(this.ir.getSymbolTable()));
				}else if(instruction instanceof SSAPhiInstruction){
					isIdentityTransfer = false;
					Hashtable<Integer, Set<PointsTo>> newTable = copy(table);
					Hashtable<Integer, Set<PointsTo>> curTable = graph.getBB()[edge.getEnd().getNumber()].getEdges().get(0).getTable();
					Enumeration<Integer> curKeys = curTable.keys();
					while(curKeys.hasMoreElements()){
						Integer curKey = curKeys.nextElement();
						Set<PointsTo> newSet = new HashSet<PointsTo>(curTable.get(curKey));
						if (newTable.get(curKey)!=null) {
							newSet.addAll(newTable.get(curKey));
						}
						newTable.remove(curKey);
						newTable.put(curKey, newSet);
					}
					newTable.remove(instruction.getDef());

					Set<PointsTo> newVariableSet = (Set<PointsTo>) new HashSet<PointsTo>();

					/*Find points of the phi node */
					for (int i=0; i< instruction.getNumberOfUses(); ++i){
						int u = instruction.getUse(i);
						if (newTable.get(u)!=null) {
							newVariableSet.addAll(newTable.get(u));
						}
					}

					newTable.put(instruction.getDef(), newVariableSet);
					for (BBEdge bbedge : endVertex.getEdges()) {
						if(isDifferent(bbedge.getTable(), newTable) && !bbedge.isMarked()){
							bbedge.setMarked(true);
							queue.add(bbedge);
						}else{
							bbedge.setMarked(false);
						}
						bbedge.setTable(newTable);
					}

					/*CONDITIONAL BRANCHES */
				}else if(instruction instanceof SSAConditionalBranchInstruction){
					Hashtable<Integer, Set<PointsTo>> newTableTrue = copy(table);
					Hashtable<Integer, Set<PointsTo>> newTableFalse = copy(table);

					if (((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.NE) || ((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.EQ)){ 
						ArrayList<BBEdge> edges = endVertex.getEdges();
						BBEdge trueEdge, falseEdge;
						if(((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.NE)){
							trueEdge = edges.get(0);
							falseEdge = edges.get(1);
						}else{
							trueEdge = edges.get(1);
							falseEdge = edges.get(0);
						}
						if(ir.getSymbolTable().getValue(instruction.getUse(1))!=null && ir.getSymbolTable().getValue(instruction.getUse(1)).isNullConstant()){
							isIdentityTransfer=false;
							int v = instruction.getUse(0);
							Set<PointsTo>ptoSet =  new HashSet<PointsTo>(trueEdge.getTable().get(v));
							boolean containsNull=false, containsNonNull=false;
							for (PointsTo pointsTo : ptoSet) {
								if(pointsTo.getIsNull()){
									containsNull=true;
									ptoSet.remove(pointsTo);
								}else{
									containsNonNull=true;
								}
							}
							/* Join with the current table */
							Hashtable<Integer, Set<PointsTo>> curTableTrue = trueEdge.getTable();
							Enumeration<Integer> curKeys = curTableTrue.keys();
							while (curKeys.hasMoreElements()) {
								Integer key = curKeys.nextElement();
								if(key==v){
									continue;
								}
								Set<PointsTo> newSet = new HashSet<PointsTo>(curTableTrue.get(key));
								if (newTableTrue.get(key)!=null) {
									newSet.addAll(newTableTrue.get(key));
								}
								newTableTrue.remove(key);
								newTableTrue.put(key, newSet);
							}

							Hashtable<Integer, Set<PointsTo>> curTableFalse = falseEdge.getTable();
							Enumeration<Integer> curKeysFalse = curTableFalse.keys();
							while (curKeysFalse.hasMoreElements()) {
								Integer key = curKeysFalse.nextElement();
								if(key==v){
									continue;
								}
								Set<PointsTo> newSet = new HashSet<PointsTo>(curTableFalse.get(key));
								if (newTableFalse.get(key)!=null) {
									newSet.addAll(newTableFalse.get(key));
								}
								newTableFalse.remove(key);
								newTableFalse.put(key, newSet);
							}
							/*End Join*/

							if(containsNonNull && containsNull){
								newTableTrue.remove(v);
								newTableTrue.put(v, ptoSet);

								trueEdge.setTable(newTableTrue);
								if(!trueEdge.isMarked() && isDifferent(newTableTrue, curTableTrue)){
									trueEdge.setMarked(true);
									queue.add(trueEdge);
								}

								newTableFalse.remove(v);
								Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
								ptoSetFalse.add(new PointsTo());
								newTableFalse.put(v, ptoSetFalse);
								falseEdge.setTable(newTableFalse);
								if(!falseEdge.isMarked() && isDifferent(newTableFalse, curTableFalse)){
									falseEdge.setMarked(true);
									queue.add(falseEdge);
								}
							}else if(containsNonNull){

								newTableTrue.remove(v);
								newTableTrue.put(v, ptoSet);
								trueEdge.setTable(newTableTrue);
								if(!trueEdge.isMarked() && isDifferent(newTableTrue, curTableTrue)){
									trueEdge.setMarked(true);
									queue.add(trueEdge);
								}


							} else if (containsNull){
								newTableFalse.remove(v);
								Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
								ptoSetFalse.add(new PointsTo());
								newTableFalse.put(v, ptoSetFalse);
								falseEdge.setTable(newTableFalse);
								if(!falseEdge.isMarked() && isDifferent(newTableFalse, curTableFalse)){
									falseEdge.setMarked(true);
									queue.add(falseEdge);
								}
							}
						}
					}
					//instruction.
				}else if(instruction instanceof SSAInvokeInstruction){
					CallSiteReference callSite = ((SSAInvokeInstruction) instruction).getCallSite();
					
					callSite.getDeclaredTarget();
					System.out.println(instruction.toString());
					for(int i=0;i<instruction.getNumberOfUses();++i){
						System.out.println(instruction.getUse(i));
					}
					System.out.println(((SSAInvokeInstruction) instruction).getCallSite());
				}
			}
			/* Identity transfer function */
			if(isIdentityTransfer){
				for (BBEdge bbedge : endVertex.getEdges()) {
					if(!bbedge.isMarked() && isDifferent(table, bbedge.getTable())){
						bbedge.setMarked(true);
						queue.add(bbedge);
					}
					bbedge.setTable(table);
				}
			}
		}
		System.out.println(graph.toString());
		return graph.getBB()[ir.getExitBlock().getNumber()-1].getEdges().get(0).getTable(); //TODO Fix?
	}
}

