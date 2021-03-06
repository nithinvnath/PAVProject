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
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
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
	//public HashMap<String, PointsToGraph> graphHistory;
	public HashMap<String, ArrayList<AnalysedMethod>> analysedMethods;
	public HashMap<Integer, Hashtable<String, Set<PointsTo>>> tableLabels;
	public HashMap<Hashtable<String, Set<PointsTo>>, Integer> labelOfTable;
	public int count=0;

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

	public void initializeAnalysedMethods(){
		this.analysedMethods = new HashMap<String, ArrayList<AnalysedMethod>>();
	}
	
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
			System.exit(1);
		}
		//this.graphHistory.put(methodName, result);
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

	private Hashtable<Integer, Hashtable<String, Set<PointsTo>>> copyCol(Hashtable<Integer, Hashtable<String, Set<PointsTo>>> col){
		Hashtable<Integer, Hashtable<String, Set<PointsTo>>> ret = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
		Set<Integer> keys = col.keySet();
		for(Integer key: keys){
			ret.put(key, copy(col.get(key)));
		}
		return ret;
	}
	
	private Set<PointsTo> copyHashSet(Set<PointsTo> ptoSet){
		HashSet<PointsTo> newSet = new HashSet<PointsTo>();
		for(PointsTo pto : ptoSet){
			PointsTo tmp = new PointsTo(pto);
			newSet.add(tmp);
		}
		return newSet;
	}
	
	private boolean isDifferent(Hashtable<String, Set<PointsTo>> oldTable, Hashtable<String, Set<PointsTo>> newTable){
		if(oldTable==null && newTable ==null){
			return false;
		}else if(oldTable ==null || newTable == null){
			return true;
		}
		Enumeration<String> oldKeys = oldTable.keys();
		Enumeration<String> newKeys = newTable.keys();
		while(newKeys.hasMoreElements() && oldKeys.hasMoreElements()){
			String newKey = newKeys.nextElement();
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

	public PointsToGraph analyseMethod(Hashtable<String, Set<PointsTo>> inpTable, String methodName){
		//Add all edges to a queue\
		//TODO blah
		Hashtable<String, Set<PointsTo>> inputTable = copy(inpTable);

		ArrayList<AnalysedMethod> analysedSet = analysedMethods.get(methodName);
		if(analysedSet==null){
			analysedSet = new ArrayList<AnalysedMethod>();
		}
		if(this.tableLabels==null){
			this.tableLabels = new HashMap<Integer, Hashtable<String, Set<PointsTo>>>();
			this.labelOfTable = new HashMap<Hashtable<String, Set<PointsTo>>, Integer>();
		}
		Set<Integer> tkeys = this.tableLabels.keySet();
		boolean tflag = false;
		for(Integer tkey : tkeys){
			if(this.tableLabels.get(tkey).equals(inputTable)){
				tflag = true;
			}
		}
		if(tflag==false){
			this.tableLabels.put(count++, inpTable);
			this.labelOfTable.put(inpTable,count-1);
		}
		
		if(analysedMethods.containsKey(methodName)){
			ArrayList<AnalysedMethod> set = analysedMethods.get(methodName);
			for (AnalysedMethod analysedMethod : set) {
				if(inputTable.equals(analysedMethod.getInputTable())){
					return analysedMethod.getGraphOutput();
				}
			}
		}
		AnalysedMethod analyMethod = new AnalysedMethod();
		analyMethod.setInputTable(inputTable);
		analysedSet.add(analyMethod);
		analysedMethods.put(methodName, analysedSet);

		PointsToGraph graph = setupGraph(methodName);
		IR ir = getCGNode(methodName).getIR();
		System.out.println(ir);
		Queue<BBEdge> queue = new LinkedList<BBEdge>();
		for (BBVertex vertex : graph.getBB()) {
			queue.addAll(vertex.getEdges());
		}

		//Iterate while queue is not empty
		while(!queue.isEmpty()){
			BBEdge edge = queue.remove();
			edge.setMarked(false);
			//Get the current table of the edge
			//Hashtable<Integer, Hashtable<Integer, Set<PointsTo>>> columnTable = copyColumnTable(edge.getColumns());
			BBVertex endVertex = graph.getBB()[edge.getEnd().getNumber()];

			if(edge.getStart().isEntryBlock()){
				/*if(!isDifferent(columnTable.get(inputTable.hashCode()), inputTable)){
					return graph;
				}
				joinToColumn(columnTable, inputTable, inputTable.hashCode());*/
				/*if(!columnLabel.containsKey(inputTable.hashCode())){
					columnLabel.put(inputTable.hashCode(),count);
					labelToTable.put(count, inputTable);
					count++;
				}*/
				edge.setTable(inputTable);
				Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
				if(curCol ==null){
					curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
				}
				curCol.remove(this.labelOfTable.get(inputTable));
				curCol.put(this.labelOfTable.get(inputTable), inputTable);
				edge.setColumns(curCol);
				edge.setMarked(false);
			}

			boolean isIdentityTransfer = true;

			List<SSAInstruction> instructions = edge.getEnd().getAllInstructions();
			for (SSAInstruction instruction : instructions) {

				/* Transfer function for "new" statements */
				if(instruction instanceof SSANewInstruction){
					isIdentityTransfer = false;
					/*Enumeration<Integer> keys = columnTable.keys();
					while (keys.hasMoreElements()) {*/
					//Integer key = keys.nextElement();
					//Applying transfer function to the existing table to make newTable
					Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
					newTable.remove(instruction.getDef());
					Set<PointsTo> newSet = (Set<PointsTo>) new HashSet<PointsTo>();
					newSet.add(new PointsTo(instruction.iindex, edge.getStart().getMethod()));
					newTable.put(((Integer)instruction.getDef()).toString(), newSet);
					/*columnTable.remove(key);
					columnTable.put(key, newTable);*/
					//Assigning the new table to successor
					for (BBEdge bbedge : endVertex.getEdges()) {
						bbedge.setTable(newTable);
						Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
						if(curCol ==null){
							curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
						}
						if(!curCol.isEmpty() && curCol.containsKey(this.labelOfTable.get(inputTable))){
							curCol.remove(this.labelOfTable.get(inputTable));
						}
						curCol.put(this.labelOfTable.get(inputTable), newTable);
						bbedge.setColumns(curCol);
						edge.setMarked(false);
						//bbedge.setColumns(columnTable);
						if (!bbedge.isMarked() && isDifferent(newTable, bbedge.getTable())) {
							bbedge.setMarked(true);
							queue.add(bbedge);
						}
					}
					//System.out.println(instruction.iindex);
					//System.out.println(instruction.toString(this.ir.getSymbolTable()));
					//}
				}else if(instruction instanceof SSAPhiInstruction){
					isIdentityTransfer = false;
					/*Enumeration<Integer> keys = columnTable.keys();
					while (keys.hasMoreElements()) {*/
//					Integer key = keys.nextElement();
					if(methodName.equals("iterativeTest")){
						System.out.println("sd");
					}
					Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
					Hashtable<String, Set<PointsTo>> curTable = graph.getBB()[edge.getEnd().getNumber()].getEdges()
							.get(0).getTable();
					if (curTable!=null) {
						Enumeration<String> curKeys = curTable.keys();
						while (curKeys.hasMoreElements()) {
							String curKey = curKeys.nextElement();
							Set<PointsTo> newSet = copyHashSet(curTable.get(curKey));
							if (newTable.get(curKey) != null) {
								newSet.addAll( copyHashSet(newTable.get(curKey)) );
							}
							newTable.remove(curKey);
							newTable.put(curKey, newSet);
						}
					}
					newTable.remove(instruction.getDef());
					Set<PointsTo> newVariableSet = (Set<PointsTo>) new HashSet<PointsTo>();
					/*Find points of the phi node */
					int flag=0;
					for (int i = 0; i < instruction.getNumberOfUses(); ++i) {
						int u = instruction.getUse(i);
						if (newTable.get(u) != null) {
							flag=1;
							newVariableSet.addAll(copyHashSet(newTable.get(u)));
						}
					}
					if(flag==1){
						newTable.put( ((Integer)instruction.getDef()).toString(), newVariableSet);
					}

					//columnTable.remove(key);
					//columnTable.put(key, newTable);

					//}
					for (BBEdge bbedge : endVertex.getEdges()) {
						if (isDifferent(curTable, newTable) && !bbedge.isMarked()) {
							bbedge.setMarked(true);
							queue.add(bbedge);
						} else {
							bbedge.setMarked(false);
						}
						bbedge.setTable(newTable);
						Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
						if(curCol ==null){
							curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
						}
						curCol.remove(this.labelOfTable.get(inputTable));
						curCol.put(this.labelOfTable.get(inputTable), newTable);
						bbedge.setColumns(curCol);
//						bbedge.setColumns(columnTable);		
					}

					/*CONDITIONAL BRANCHES */
				}else if(instruction instanceof SSAConditionalBranchInstruction){
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

						Hashtable<String, Set<PointsTo>> newTableTrue = copy(edge.getTable());
						Hashtable<String, Set<PointsTo>> newTableFalse = copy(edge.getTable());
						if (ir.getSymbolTable().getValue(instruction.getUse(1)) != null
								&& ir.getSymbolTable().getValue(instruction.getUse(1)).isNullConstant()) {
							isIdentityTransfer = false;
							int v = instruction.getUse(0);
							boolean containsNull = false, containsNonNull = false;
							Set<PointsTo> ptoSet = null;
							if(edge.getTable().isEmpty() || edge.getTable().get(v)==null){
								containsNull = false;
								containsNonNull = false;
							}else{
								ptoSet = new HashSet<PointsTo>(edge.getTable().get(v));
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
							Hashtable<String, Set<PointsTo>> curTableTrue = trueEdge.getTable();
							if(curTableTrue==null){
								curTableTrue = new Hashtable<String, Set<PointsTo>>();
							}
							Enumeration<String> curKeys = curTableTrue.keys();
							while (curKeys.hasMoreElements()) {
								String curKey = curKeys.nextElement();
								if (curKey.equals(((Integer)v).toString())) {
									continue;
								}
								Set<PointsTo> newSet = copyHashSet(curTableTrue.get(curKey));
								if (newTableTrue.get(curKey) != null) {
									newSet.addAll(copyHashSet(newTableTrue.get(curKey)));
								}
								newTableTrue.remove(curKey);
								newTableTrue.put(curKey, newSet);
							}

							Hashtable<String, Set<PointsTo>> curTableFalse = falseEdge.getTable();
							if(curTableFalse==null){
								curTableFalse = new Hashtable<String, Set<PointsTo>>();
							}
							Enumeration<String> curKeysFalse = curTableFalse.keys();
							while (curKeysFalse.hasMoreElements()) {
								String curKey = curKeysFalse.nextElement();
								if (curKey.equals(((Integer)v).toString())) {
									continue;
								}
								Set<PointsTo> newSet = copyHashSet(curTableFalse.get(curKey));
								if (newTableFalse.get(curKey) != null) {
									newSet.addAll( copyHashSet(newTableFalse.get(curKey)) );
								}
								newTableFalse.remove(curKey);
								newTableFalse.put(curKey, newSet);
							}

							/*End Join*/

							if (containsNonNull && containsNull) {
								newTableTrue.remove(""+v);
								newTableTrue.put( ""+v , ptoSet);

								trueEdge.setTable(newTableTrue);

								newTableFalse.remove(""+v);
								Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
								ptoSetFalse.add(new PointsTo());
								newTableFalse.put(""+v, ptoSetFalse);
								falseEdge.setTable(newTableFalse);
							} else if (containsNonNull) {

								newTableTrue.remove(""+v);
								newTableTrue.put(""+v, ptoSet);
								trueEdge.setTable(newTableTrue);

							} else if (containsNull) {
								newTableFalse.remove(""+v);
								Set<PointsTo> ptoSetFalse = new HashSet<PointsTo>();
								ptoSetFalse.add(new PointsTo());
								newTableFalse.put(""+v, ptoSetFalse);
								falseEdge.setTable(newTableFalse);
								trueEdge.setTable(new Hashtable<String, Set<PointsTo>>());
							}else{
								falseEdge.setTable(new Hashtable<String, Set<PointsTo>>());
								trueEdge.setTable(newTableFalse);
							}
							Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(trueEdge.getColumns());
							if(curCol ==null){
								curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
							}
							curCol.remove(this.labelOfTable.get(inputTable));
							curCol.put(this.labelOfTable.get(inputTable), newTableTrue);
							trueEdge.setColumns(curCol);

							Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curColF = copyCol(falseEdge.getColumns());
							if(curColF ==null){
								curColF = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
							}
							curColF.remove(this.labelOfTable.get(inputTable));
							curColF.put(this.labelOfTable.get(inputTable), newTableFalse);
							trueEdge.setColumns(curColF);
							
						}
						//instruction.
					}
				}else if(instruction instanceof SSAInvokeInstruction){
					CallSiteReference callSite = ((SSAInvokeInstruction) instruction).getCallSite();
					
					if (!callSite.isSpecial() && !callSite.getDeclaredTarget().getName().toString().equals("toString")) {
						isIdentityTransfer=false;
						
						System.out.println(callSite.getDeclaredTarget().getName().toString());
						
						//If the function call contains some arguments, add it to the table
						String funcName = callSite.getDeclaredTarget().getName().toString();
						Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
						Hashtable<String, Set<PointsTo>> tmp = new Hashtable<String, Set<PointsTo>>();
						for (int i = 0; i < instruction.getNumberOfUses(); i++) {
							if(!callSite.isStatic() && i==0){
								continue; //First argument will be 'this' for non static function calls 
							}
							int v = instruction.getUse(i);
							HashSet<PointsTo> nullPointToSet = new HashSet<PointsTo>();
							nullPointToSet.add(new PointsTo());
							if (ir.getSymbolTable().getValue(v) != null
									&& ir.getSymbolTable().getValue(v).isNullConstant()) {
								tmp.put(""+v, nullPointToSet);
								if(!newTable.containsKey(v)){
									newTable.put(""+v, nullPointToSet);
								}
							} 
						}
						PointsToGraph returnGraph = analyseMethod(tmp, funcName);
						
						if(instruction.getNumberOfDefs()!=0 && instruction.getDef()!=-1){
							Set<PointsTo> resultTable = newTable.get(instruction.getDef());
							if(resultTable ==null){
								resultTable = new HashSet<PointsTo>();
							}
							for(Integer val : returnGraph.returnValIndex){
								if(returnGraph.returnTable.get(val)!=null){
									resultTable.addAll( copyHashSet(returnGraph.returnTable.get(val)));
								}
							}
							
							newTable.remove(""+instruction.getDef());
							newTable.put(""+instruction.getDef(), resultTable);
						}
						System.out.println(funcName+": \n"+returnGraph.toString()+"---------------------------------\n");
						
						for (BBEdge bbedge : endVertex.getEdges()) {
							if (!bbedge.isMarked() && isDifferent(newTable, bbedge.getTable())) {
								bbedge.setMarked(true);
								queue.add(bbedge);
							}
							bbedge.setTable(newTable);
							Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
							if(curCol ==null){
								curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
							}
							curCol.remove(this.labelOfTable.get(inputTable));
							curCol.put(this.labelOfTable.get(inputTable), newTable);
							bbedge.setColumns(curCol);
							//System.out.println(bbedge);
						} 
					}
					//System.out.println(graph);

				}else if(instruction instanceof SSAReturnInstruction){
					Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
					//If statement is return null, we need to add v9 -> {null} to next edge
					if(instruction.getNumberOfUses()!=0){
						int v = instruction.getUse(0);
						if (!newTable.containsKey(v) && ( ir.getSymbolTable().getValue(v) == null
								|| ir.getSymbolTable().getValue(v).isNullConstant() ) ) {
							isIdentityTransfer = false;
							HashSet<PointsTo> nullPointToSet = new HashSet<PointsTo>();
							nullPointToSet.add(new PointsTo());
							newTable.put(""+v, nullPointToSet);
						}
						for (BBEdge bbedge : endVertex.getEdges()) {
							if (!bbedge.isMarked() && isDifferent(newTable, bbedge.getTable())) {
								bbedge.setMarked(true);
								queue.add(bbedge);
							}
							bbedge.setTable(newTable);
							Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
							if(curCol ==null){
								curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
							}
							curCol.remove(this.labelOfTable.get(inputTable));
							curCol.put(this.labelOfTable.get(inputTable), newTable);
							bbedge.setColumns(curCol);
						}
					}
					if (graph.returnTable==null) {
						graph.returnTable = newTable;
					}else{
						HashSet<PointsTo> pointsToSet = new HashSet<PointsTo>();
						//Join returnTable with newTable
						Enumeration<String> keys = newTable.keys();
						while(keys.hasMoreElements()){
							String key = keys.nextElement();
							pointsToSet.addAll(copyHashSet(newTable.get(key)));
							if (graph.returnTable.containsKey(key)) {
								pointsToSet.addAll(copyHashSet(graph.returnTable.get(key)));
							}
							graph.returnTable.remove(key);
							graph.returnTable.put(key.toString(), pointsToSet);
						}
					}
					if(instruction.getNumberOfUses() > 0){
						graph.returnValIndex.add(instruction.getUse(0));
					}
				}else if(instruction instanceof SSAPutInstruction){
					isIdentityTransfer=false;
					int v1 = instruction.getUse(0);
					int v2 = instruction.getUse(2);
					String f = ((SSAPutInstruction) instruction).getDeclaredField().getName().toString();
					Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
					Set<PointsTo> newSet = newTable.get(""+v2);
					Set<PointsTo> v1Set = newTable.get(""+v1);
					for(PointsTo v : v1Set){
						PointsTo x = new PointsTo(v);
						x.setField(f);
						newTable.remove(x.toString());
						newTable.put(x.toString(), newSet);
					}
					/*columnTable.remove(key);
					columnTable.put(key, newTable);*/
					//Assigning the new table to successor
					for (BBEdge bbedge : endVertex.getEdges()) {
						bbedge.setTable(newTable);
						Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
						if(curCol ==null){
							curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
						}
						if(!curCol.isEmpty() && curCol.containsKey(this.labelOfTable.get(inputTable))){
							curCol.remove(this.labelOfTable.get(inputTable));
						}
						curCol.put(this.labelOfTable.get(inputTable), newTable);
						bbedge.setColumns(curCol);
						edge.setMarked(false);
						//bbedge.setColumns(columnTable);
						if (!bbedge.isMarked() && isDifferent(newTable, bbedge.getTable())) {
							bbedge.setMarked(true);
							queue.add(bbedge);
						}
					}
				}else if(instruction instanceof SSAGetInstruction){
					isIdentityTransfer=false;
					int v1 = instruction.getDef(0);
					int v2 = instruction.getUse(0);
					String f = ((SSAGetInstruction) instruction).getDeclaredField().getName().toString();
					Hashtable<String, Set<PointsTo>> newTable = copy(edge.getTable());
					Set<PointsTo> v2Set = newTable.get(""+v2);
					Set<PointsTo> newSet = new HashSet<PointsTo>();
					for(PointsTo v : v2Set){
						String key = v.toString()+"."+f;
						if(newTable.containsKey(key)){
							newSet.addAll(newTable.get(key));
						}
					}
					newTable.remove(""+v1);
					newTable.put(""+v1, newSet);
					for (BBEdge bbedge : endVertex.getEdges()) {
						bbedge.setTable(newTable);
						Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
						if(curCol ==null){
							curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
						}
						if(!curCol.isEmpty() && curCol.containsKey(this.labelOfTable.get(inputTable))){
							curCol.remove(this.labelOfTable.get(inputTable));
						}
						curCol.put(this.labelOfTable.get(inputTable), newTable);
						bbedge.setColumns(curCol);
						edge.setMarked(false);
						//bbedge.setColumns(columnTable);
						if (!bbedge.isMarked() && isDifferent(newTable, bbedge.getTable())) {
							bbedge.setMarked(true);
							queue.add(bbedge);
						}
					}
				}
			}
			/* Identity transfer function */
			if(isIdentityTransfer){
				for (BBEdge bbedge : endVertex.getEdges()) {
					if (!bbedge.isMarked() && isDifferent(edge.getTable(), bbedge.getTable())) {
						bbedge.setMarked(true);
						queue.add(bbedge);
					}
					bbedge.setTable(edge.getTable());
					Hashtable<Integer, Hashtable<String, Set<PointsTo>>> curCol = copyCol(edge.getColumns());
					if(curCol ==null){
						curCol = new Hashtable<Integer, Hashtable<String, Set<PointsTo>>>();
					}
					curCol.remove(this.labelOfTable.get(inputTable));
					curCol.put(this.labelOfTable.get(inputTable), bbedge.getTable());
					bbedge.setColumns(curCol);
//					bbedge.setColumns(columnTable);
				} 

			}
		}
		//System.out.println(graph);
		this.analysedMethods.remove(methodName);
		analysedSet.remove(analyMethod);
		analyMethod.setGraphOutput(new PointsToGraph(graph));
		analysedSet.add(analyMethod);
		this.analysedMethods.put(methodName, analysedSet);
		
		return graph; 
	}
}

