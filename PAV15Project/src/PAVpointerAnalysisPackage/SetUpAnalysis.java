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
	private HashMap<String, PointsToGraph> graphHistory;

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
		if(this.graphHistory==null){
			this.graphHistory = new HashMap<String, PointsToGraph>();
		}
		if(this.graphHistory.containsKey(methodName)){
			return this.graphHistory.get(methodName);
		}
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
		this.graphHistory.put(methodName, result);
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

	private Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> copyColumnTable(Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> source){
		Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> newColumn = new Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>>();
		Enumeration<Integer> keys = source.keys();
		while(keys.hasMoreElements()){
			Integer key = keys.nextElement();
			newColumn.put(key, copy(source.get(key)));
		}
		return newColumn;
	}

	private boolean isDifferent(Hashtable<Integer, Set<PointsTo>> oldTable, Hashtable<Integer, Set<PointsTo>> newTable){
		if(oldTable==null && newTable ==null){
			return false;
		}else if(oldTable ==null || newTable == null){
			return true;
		}
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

	private boolean isDifferentColumns(Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> column1, Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> column2){
		Enumeration<Integer> col1Keys = column1.keys();
		Enumeration<Integer> col2Keys = column2.keys();
		while(col1Keys.hasMoreElements() && col2Keys.hasMoreElements()){
			Integer col2Key = col2Keys.nextElement();
			col1Keys.nextElement();
			if(!column1.containsKey(col2Key)){
				return true;
			}
			if(isDifferent(column1.get(col2Key), column2.get(col2Key)) ){
				return true;
			}
		}
		if(col1Keys.hasMoreElements() || col2Keys.hasMoreElements()){
			return true;
		}
		return false;
	}

	private void joinToColumn(Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> columns, Hashtable<Integer, Set<PointsTo>> inputTable, Integer columnNum){
		if(columns.containsKey(columnNum)){
			Hashtable<Integer, Set<PointsTo>> newTable = copy(inputTable);
			Hashtable<Integer, Set<PointsTo>> curTable = columns.get(columnNum);
			Enumeration<Integer> keys = curTable.keys();
			while(keys.hasMoreElements()){
				Integer key = keys.nextElement();
				Set<PointsTo> newSet = new HashSet<PointsTo>(curTable.get(key));
				if (newTable.get(key)!=null) {
					newSet.addAll(newTable.get(key));
				}
				newTable.remove(key);
				newTable.put(key, newSet);
			}
		}else{
			columns.put(columnNum, inputTable);
		}
	}

	private Hashtable<Integer, Set<PointsTo>> getJoinOfColumns(Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> columnTable){
		Hashtable<Integer, Set<PointsTo>> result = new Hashtable<Integer, Set<PointsTo>>();
		Enumeration<Integer> keys = columnTable.keys();
		while(keys.hasMoreElements()){
			Integer key = keys.nextElement();
			Hashtable<Integer, Set<PointsTo>> cur = columnTable.get(key);
			Enumeration<Integer> curKeys = cur.keys();
			while(curKeys.hasMoreElements()){
				Integer curKey = curKeys.nextElement();
				if(result.get(curKey)==null){
					result.put(curKey, cur.get(curKey));
				}else{
					Set<PointsTo> newSet = new HashSet<PointsTo>(cur.get(curKey));
					newSet.addAll(result.get(curKey));
					result.remove(curKey);
					result.put(curKey, newSet);
				}
			}
		}
		return result;
	}

	public PointsToGraph analyseMethod(Hashtable<Integer, Set<PointsTo>> inputTable, String methodName){
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
			Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> columnTable = copyColumnTable(edge.getColumns());
			Hashtable<Integer, Set<PointsTo>> table = edge.getTable();
			BBVertex endVertex = graph.getBB()[edge.getEnd().getNumber()];

			if(edge.getStart().isEntryBlock()){
				if(!isDifferent(columnTable.get(inputTable.hashCode()), inputTable)){
					return graph;
				}
				joinToColumn(columnTable, inputTable, inputTable.hashCode());
				edge.setTable(inputTable);
				edge.setMarked(false);
			}

			boolean isIdentityTransfer = true;

			List<SSAInstruction> instructions = edge.getEnd().getAllInstructions();
			for (SSAInstruction instruction : instructions) {

				/* Transfer function for "new" statements */
				if(instruction instanceof SSANewInstruction){
					isIdentityTransfer = false;
					Enumeration<Integer> keys = columnTable.keys();
					while (keys.hasMoreElements()) {
						Integer key = keys.nextElement();
						//Applying transfer function to the existing table to make newTable
						Hashtable<Integer, Set<PointsTo>> newTable = copy(columnTable.get(key));
						newTable.remove(instruction.getDef());
						Set<PointsTo> newSet = (Set<PointsTo>) new HashSet<PointsTo>();
						newSet.add(new PointsTo(instruction.iindex, edge.getStart().getMethod()));
						newTable.put(instruction.getDef(), newSet);
						columnTable.remove(key);
						columnTable.put(key, newTable);
						//Assigning the new table to successor
						for (BBEdge bbedge : endVertex.getEdges()) {
							bbedge.setTable(newTable);
							bbedge.setColumns(columnTable);
							if (!bbedge.isMarked()) {
								bbedge.setMarked(true);
								queue.add(bbedge);
							}
						}
						//System.out.println(instruction.iindex);
						//System.out.println(instruction.toString(this.ir.getSymbolTable()));
					}
				}else if(instruction instanceof SSAPhiInstruction){
					isIdentityTransfer = false;
					Enumeration<Integer> keys = columnTable.keys();
					while (keys.hasMoreElements()) {
						Integer key = keys.nextElement();

						Hashtable<Integer, Set<PointsTo>> newTable = copy(columnTable.get(key));
						Hashtable<Integer, Set<PointsTo>> curTable = graph.getBB()[edge.getEnd().getNumber()].getEdges()
								.get(0).getColumns().get(key);
						if (curTable!=null) {
							Enumeration<Integer> curKeys = curTable.keys();
							while (curKeys.hasMoreElements()) {
								Integer curKey = curKeys.nextElement();
								Set<PointsTo> newSet = new HashSet<PointsTo>(curTable.get(curKey));
								if (newTable.get(curKey) != null) {
									newSet.addAll(newTable.get(curKey));
								}
								newTable.remove(curKey);
								newTable.put(curKey, newSet);
							} 
						}
						newTable.remove(instruction.getDef());
						Set<PointsTo> newVariableSet = (Set<PointsTo>) new HashSet<PointsTo>();
						/*Find points of the phi node */
						for (int i = 0; i < instruction.getNumberOfUses(); ++i) {
							int u = instruction.getUse(i);
							if (newTable.get(u) != null) {
								newVariableSet.addAll(newTable.get(u));
							}
						}
						newTable.put(instruction.getDef(), newVariableSet);
						columnTable.remove(key);
						columnTable.put(key, newTable);

					}
					for (BBEdge bbedge : endVertex.getEdges()) {
						if (isDifferentColumns(bbedge.getColumns(), columnTable) && !bbedge.isMarked()) {
							bbedge.setMarked(true);
							queue.add(bbedge);
						} else {
							bbedge.setMarked(false);
						}
						bbedge.setTable(getJoinOfColumns(columnTable));
						bbedge.setColumns(columnTable);		
					}

					/*CONDITIONAL BRANCHES */
				}else if(instruction instanceof SSAConditionalBranchInstruction){
					Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> newColumnTableTrue = copyColumnTable(columnTable);
					Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> newColumnTableFalse = copyColumnTable(columnTable);


					if (((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.NE)
							|| ((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.EQ)) {
						ArrayList<BBEdge> edges = endVertex.getEdges();
						BBEdge trueEdge, falseEdge;
						if (((SSAConditionalBranchInstruction) instruction).getOperator().equals(Operator.NE)) {
							trueEdge = edges.get(0);
							falseEdge = edges.get(1);
						} else {
							trueEdge = edges.get(1);
							falseEdge = edges.get(0);
						}
						Enumeration<Integer> keys = columnTable.keys();
						while (keys.hasMoreElements()) {
							Integer key =keys.nextElement();
							Hashtable<Integer, Set<PointsTo>> newTableTrue = copy(newColumnTableTrue.get(key));
							Hashtable<Integer, Set<PointsTo>> newTableFalse = copy(newColumnTableFalse.get(key));
							if (ir.getSymbolTable().getValue(instruction.getUse(1)) != null
									&& ir.getSymbolTable().getValue(instruction.getUse(1)).isNullConstant()) {
								isIdentityTransfer = false;
								int v = instruction.getUse(0);
								boolean containsNull = false, containsNonNull = false;
								Set<PointsTo> ptoSet = null;
								if(edge.getColumns().get(key).isEmpty() || edge.getColumns().get(key).get(v)==null){
									containsNull = false;
									containsNonNull = false;
								}else{
									ptoSet = new HashSet<PointsTo>(edge.getColumns().get(key).get(v));
									for (PointsTo pointsTo : ptoSet) {
										if (pointsTo.getIsNull()) {
											containsNull = true;
											ptoSet.remove(pointsTo);
										} else {
											containsNonNull = true;
										}
									}
								}
								/* Join with the current table */
								Hashtable<Integer, Set<PointsTo>> curTableTrue = trueEdge.getColumns().get(key);
								if(curTableTrue==null){
									curTableTrue = new Hashtable<Integer, Set<PointsTo>>();
								}
								Enumeration<Integer> curKeys = curTableTrue.keys();
								while (curKeys.hasMoreElements()) {
									Integer curKey = curKeys.nextElement();
									if (curKey == v) {
										continue;
									}
									Set<PointsTo> newSet = new HashSet<PointsTo>(curTableTrue.get(curKey));
									if (newTableTrue.get(curKey) != null) {
										newSet.addAll(newTableTrue.get(curKey));
									}
									newTableTrue.remove(curKey);
									newTableTrue.put(curKey, newSet);
								}

								Hashtable<Integer, Set<PointsTo>> curTableFalse = falseEdge.getColumns().get(key);
								if(curTableFalse==null){
									curTableFalse = new Hashtable<Integer, Set<PointsTo>>();
								}
								Enumeration<Integer> curKeysFalse = curTableFalse.keys();
								while (curKeysFalse.hasMoreElements()) {
									Integer curKey = curKeysFalse.nextElement();
									if (curKey == v) {
										continue;
									}
									Set<PointsTo> newSet = new HashSet<PointsTo>(curTableFalse.get(curKey));
									if (newTableFalse.get(curKey) != null) {
										newSet.addAll(newTableFalse.get(curKey));
									}
									newTableFalse.remove(curKey);
									newTableFalse.put(curKey, newSet);
								}
								newColumnTableFalse.remove(key);
								newColumnTableFalse.put(key, newTableTrue);
								/*End Join*/

								if (containsNonNull && containsNull) {
									newTableTrue.remove(v);
									newTableTrue.put(v, ptoSet);

									trueEdge.setTable(newTableTrue);

									newTableFalse.remove(v);
									Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
									ptoSetFalse.add(new PointsTo());
									newTableFalse.put(v, ptoSetFalse);
									falseEdge.setTable(newTableFalse);
								} else if (containsNonNull) {

									newTableTrue.remove(v);
									newTableTrue.put(v, ptoSet);
									trueEdge.setTable(newTableTrue);

								} else if (containsNull) {
									newTableFalse.remove(v);
									Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
									ptoSetFalse.add(new PointsTo());
									newTableFalse.put(v, ptoSetFalse);
									falseEdge.setTable(newTableFalse);

								}
							}
						}
						if (!trueEdge.isMarked() && isDifferentColumns(newColumnTableTrue, trueEdge.getColumns())) {
							trueEdge.setMarked(true);
							queue.add(trueEdge);
						}
						if(isDifferentColumns(newColumnTableTrue, trueEdge.getColumns())){
							trueEdge.setColumns(newColumnTableTrue);
							trueEdge.setTable(getJoinOfColumns(newColumnTableTrue));
						}

						if (!falseEdge.isMarked() && isDifferentColumns(newColumnTableFalse, falseEdge.getColumns())) {
							falseEdge.setMarked(true);
							queue.add(falseEdge);
						}
						if(isDifferentColumns(newColumnTableFalse, falseEdge.getColumns())){
							falseEdge.setColumns(newColumnTableFalse);
							falseEdge.setTable(getJoinOfColumns(newColumnTableFalse));
						}
						//instruction.
					}
				}else if(instruction instanceof SSAInvokeInstruction){
					CallSiteReference callSite = ((SSAInvokeInstruction) instruction).getCallSite();
					Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> newColumnTable = copyColumnTable(columnTable);
					if (!callSite.isSpecial() && !callSite.isVirtual()) {
						isIdentityTransfer=false;
						Enumeration<Integer> keys = columnTable.keys();
						System.out.println(callSite.getDeclaredTarget().getName().toString());
						while (keys.hasMoreElements()) {
							Integer key = keys.nextElement();
							PointsToGraph returnGraph;
							String funcName = callSite.getDeclaredTarget().getName().toString();
							int v = instruction.getUse(0);
							HashSet<PointsTo> nullPointToSet = new HashSet<PointsTo>();
							nullPointToSet.add(new PointsTo());
							Hashtable<Integer, Set<PointsTo>> temp = copy(columnTable.get(key));
							if(ir.getSymbolTable().getValue(v).isNullConstant()){
								temp.remove(v);
								temp.put(v, nullPointToSet);
								columnTable.remove(key);
								columnTable.put(key, temp);
							}
							returnGraph = analyseMethod(columnTable.get(key), funcName);
							System.out.println(funcName+": \n"+returnGraph.toString()+"---------------------------------\n");
							Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> returnColumnTable = returnGraph.getBB()[getCGNode(funcName).getIR().getExitBlock().getNumber()-1].getEdges().get(0).getColumns();
							Hashtable<Integer, Set<PointsTo>> colTable = returnColumnTable.get(columnTable.get(key).hashCode());
							if(colTable!=null){
								newColumnTable.remove(key);
								newColumnTable.put(key, colTable);
							}
							graphHistory.put(callSite.getDeclaredTarget().getName().toString(), returnGraph);
						}
						for (BBEdge bbedge : endVertex.getEdges()) {
							if (!bbedge.isMarked() && isDifferentColumns(columnTable, bbedge.getColumns())) {
								bbedge.setMarked(true);
								queue.add(bbedge);
							}
							bbedge.setTable(getJoinOfColumns(newColumnTable));
							Enumeration<Integer> newKeys = newColumnTable.keys();
							while(newKeys.hasMoreElements()){
								Integer newKey = newKeys.nextElement();
								joinToColumn(bbedge.getColumns(), newColumnTable.get(newKey), newColumnTable.get(newKey).hashCode());
							}
							//(bbedge.getColumns(), newColumnTable, newColumnTable.hashCode());
							bbedge.setColumns(newColumnTable);
							//System.out.println(bbedge);
							System.out.println(graph);
						} 
					}

				}
			}
			/* Identity transfer function */
			if(isIdentityTransfer){
				for (BBEdge bbedge : endVertex.getEdges()) {
					if (!bbedge.isMarked() && isDifferentColumns(columnTable, bbedge.getColumns())) {
						bbedge.setMarked(true);
						queue.add(bbedge);
					}
					bbedge.setTable(getJoinOfColumns(columnTable));
					bbedge.setColumns(columnTable);
				} 

			}
		}
		//System.out.println(graph);
		return graph; //TODO Fix?
	}
}

