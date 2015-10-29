package PAVpointerAnalysisPackage;


/**
 * @author Suvam Mukherjee
 * Program Analysis and Verification, 2013
 *
 */

//Do NOT import the slicer package
import java.io.*;
import java.util.*;

import PAVpointerAnalysisPackage.PAVPointerAnalysis;
import PAVpointerAnalysisPackage.SetUpAnalysis;

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
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;


public class PAVPointerAnalysis {

	private SetUpAnalysis setup;	// Object to setup (check presentation)
	private String rootMethod;
	
	public PAVPointerAnalysis(String classpath, String mainClass, String analysisClass, String analysisMethod)
	{
		setup = new SetUpAnalysis(classpath, mainClass, analysisClass, analysisMethod);
		this.rootMethod = analysisMethod;
	}
	
	public void runAnalysis() throws Exception
	{
		/**
		 * Use setup to construct the relevant data structures (cfg, etc).
		 * and to perform analysis
		 * Print out analysis results to file, in the format specified.
		 */
		
		//START: NO CHANGE REGION
		setup.buildScope();
		setup.buildClassHierarchy();
		setup.buildEntryPoints();
		setup.setUpCallGraphConstruction();
		setup.generateCallGraph();
		//END: NO CHANGE REGION
		
		/*
		 * For demonstration purposes, and to help you check whether Wala is setup properly, I am calling the setup.printNodes() function.
		 * This will compute the call graph for the given program, and print out the nodes. Feel free to erase this call, or use it otherwise. 
		 */
		//setup.printNodes();
		//setup.printIR();
		PointsToGraph result = setup.analyseMethod(new Hashtable<Integer, Set<PointsTo>>(), this.rootMethod);
		System.out.println(result.toString());
		writeOutput(setup.graphHistory);
		
		/*
		 * Create appropriate objects/make appropriate function calls here to begin the analysis
		 */
	}
	
	private void writeOutput(HashMap<String, PointsToGraph> graphSet) throws IOException{
		File file = new File("./ColumnOutput");
		File file2 = new File("./JoinOutput");
		if (!file.exists()) {
			file.createNewFile();
		}
		StringBuffer output = new StringBuffer();
		StringBuffer output2 = new StringBuffer();
		Set<Integer> labelKeys = this.setup.columnLabel.keySet();
		output.append("Columns: \n");
		for (Integer lkey : labelKeys) {
			int index = this.setup.columnLabel.get(lkey);
			output.append("C"+index+":\n");
			Hashtable<Integer, Set<PointsTo>> z = this.setup.labelToTable.get(index);
			Enumeration<Integer> zkeys = z.keys();
			while(zkeys.hasMoreElements()){
				Integer zkey = zkeys.nextElement();
				output.append(" {v" + zkey + " -> {");
				Set<PointsTo> p = z.get(zkey);
				for (Iterator<PointsTo> iterator = p.iterator(); iterator.hasNext();) {
					PointsTo pto = (PointsTo) iterator.next();
					output.append(pto.toString());
					if (iterator.hasNext()) {
						output.append(", ");
					}
				}output.append("}}\n");
			}
			//output.append(this.setup.labelToTable.get(index).toString()+"\n");
		}
		output.append("\n");
		Set<String> keys = graphSet.keySet();
		for (String key : keys) {
			output.append("\n"+key+":\n");
			output2.append("\n"+key+":\n");
			output.append(graphSet.get(key).printTables(this.setup.columnLabel));
			output2.append(graphSet.get(key).toString());
			output.append("\n\n");
			output2.append("\n\n");
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		FileWriter fw2 = new FileWriter(file2.getAbsoluteFile());
		BufferedWriter bw2 = new BufferedWriter(fw2);
		bw2.write(output2.toString());
		bw.write(output.toString());
		bw.close();
		bw2.close();
		System.out.println("Written to file!");
	}
	
	// START: NO CHANGE REGION
	/**
	 * The main method reads in the arguments from the command line and sets up the appropriate variables.
	 * @param args
	 * args[0]: Full path of the application jar file. 
	 * args[1]: Fully qualified name of the class containing the main method. The name is of the form L<package_name>/<class_name>.
	 * 			For example, to analyze the class test1 in package tests, the argument would be Ltests/test1
	 * args[2]: Fully qualified name of the class containing the method which we want to analyze.
	 * args[3]: Name of the method we want to analyze.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		
		String classpath, mainClass, analysisClass, analysisMethod;
				
		classpath = args[0];
		mainClass = args[1];
		analysisClass = args[2];
		analysisMethod = args[3];
		
		
		PAVPointerAnalysis pAnalysis = new PAVPointerAnalysis(classpath, mainClass, analysisClass, 
										analysisMethod);
		
		pAnalysis.runAnalysis();
		
	}
	// END: NO CHANGE REGION
}
