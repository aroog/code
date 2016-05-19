package edu.wayne.summary.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import oog.itf.IElement;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.wayne.summary.strategies.EdgeSummaryAll;
import edu.wayne.summary.strategies.EdgeSummaryAllX;
import edu.wayne.summary.strategies.EdgeSummaryEdgeCount;
import edu.wayne.summary.strategies.EdgeSummaryIn;
import edu.wayne.summary.strategies.EdgeSummaryInX;
import edu.wayne.summary.strategies.EdgeSummaryObjCreationCount;
import edu.wayne.summary.strategies.EdgeSummaryOut;
import edu.wayne.summary.strategies.EdgeSummaryOutX;
import edu.wayne.summary.strategies.HierarchySummary;
import edu.wayne.summary.strategies.Info;
import edu.wayne.summary.strategies.SummaryImpl1;
import edu.wayne.summary.strategies.SummaryStrategy;
import edu.wayne.summary.strategies.Utils;
import edu.wayne.summary.utils.JavaElementUtils;

// Test class for testing OOG-based summaries on MiniDraw
// NOTE: this is not using JUnit, because we want to run the code based on the project in the Eclipse child instance
// TODO: Store the expected list of MICs for MD
// TODO: Store the expected list of relatedMICs for a class in MD
// TODO: Store the expected list of MIMs for a class in MD
public class MD {

	/*public void testFacade() {
		System.out.println("Testing Most Important Classes");

		Facade facade = FacadeImpl.getInstance();

		// Get the actual result
		Set<String> setMICs = facade.getMostImportantClasses();
		// TODO: compare to expected result
		// For now, just check that we got something
		Assert.assertNotNull(setMICs);
		Assert.assertTrue(setMICs.size() > 0);

		// Crystal crystal = Crystal.getInstance();
		// ITypeBinding typeBinding1 =
		// crystal.getTypeBindingFromName("minidraw.breakthrough.GameStub");

		System.out.println("Testing Most Important Classes Related To Class");
		// Get the actual result
		Set<String> setMIRCs = facade.getMostImportantClassesRelatedToClass(
				"minidraw.breakthrough.GameStub", RelationshipType.All);

		// TODO: Compare to expected result
		// For now, just check that we got something
		Assert.assertTrue(setMIRCs.size() > 0);

		System.out.println("Testing Most Important Methods Of Class");
		Set<String> setMIMs = facade
				.getMostImportantMethodsOfClass("minidraw.breakthrough.GameStub");
		// TODO: Compare to expected result
		// For now, just check that we got something
		Assert.assertTrue(setMIMs.size() > 0);
	}*/

	public <T> void testStrategy(SummaryStrategy<T> summary, IPath debugPath)
			throws IOException, JavaModelException {
		IJavaProject javaProject = JavaElementUtils.getJavaProject();
		summary.compute();

		String strategyName = summary.getClass().getSimpleName();
		IPath strategyPath = debugPath.append(strategyName);
		
		IProject project = javaProject.getProject();
		IFolder folder = project.getFolder("debug").getFolder(strategyName);
		try {
			if(!folder.exists())
			folder.create(false, true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String micFileName = "MIC_"+ strategyName+".txt";
		IPath micPath = debugPath.append(micFileName);

		String micFile = micPath.toOSString();

		FileWriter out = new FileWriter(micFile);

		out.append("Most Important Classes\n");
		// Get the actual result
		Set<Info<T>> setMICs = summary.getMostImportantClasses();
		printSet(setMICs, out);
		out.flush();
		out.close();

		for (String javaClass : summary.getAllClasses()) {
			String simpleName = Signature.getSimpleName(javaClass);
			String mircFileName = simpleName+"_MIRC"+ ".txt";
			IPath mircFilePath = strategyPath.append(mircFileName);
			String mircFile = mircFilePath.toOSString();
			FileWriter mirc_writer = new FileWriter(mircFile);
			
			mirc_writer.append("Most Important Classes Related To Class ");
			mirc_writer.append(javaClass);
			mirc_writer.append("\n");
			Set<Info<T>> setMIRCs = summary.getMostImportantRelatedClass(javaClass);
			printSet(setMIRCs, mirc_writer);
			mirc_writer.flush();
			mirc_writer.close();
			
			
			String mimFileName = simpleName+"_MIM"+ ".txt";
			IPath mimFilePath = strategyPath.append(mimFileName);
			String mimFile = mimFilePath.toOSString();
			FileWriter mim_writer = new FileWriter(mimFile);
			
			mim_writer.append("Testing Most Important Methods To Class ");
			mim_writer.append(javaClass);
			mim_writer.append("\n");

			Set<Info<T>> setMIMs = summary.getMostImportantMethods(javaClass);
			double totalMIMs = setMIMs.size();
			double totalNumMethods = Utils.getCompatibleMethodSize(javaClass);
			if(totalNumMethods>0)
				mim_writer.append("Precision Factor:" + totalMIMs + "/"+ totalNumMethods 
					+ "= " + (totalMIMs/totalNumMethods) + "\n");

			printSet(setMIMs, mim_writer);
			mim_writer.flush();
			mim_writer.close();
		}
		
		// XXX. Revisit this. must select fields.
		for(String itf:summary.getAllInterfaces()){
			String simpleName = Signature.getSimpleName(itf);
			String cbiFileName = simpleName+"_CBI"+ ".txt";
			IPath cbiFilePath = strategyPath.append(cbiFileName);
			String cbiFile = cbiFilePath.toOSString();
			FileWriter cbi_writer = new FileWriter(cbiFile);
			cbi_writer.append("Most Important Classes Behind Interface ");
			cbi_writer.append(itf);
			cbi_writer.append("\n");
			// XXX. Fix me.
			//printSet(summary.getClassesBehindInterface(itf), cbi_writer);
			cbi_writer.flush();
			cbi_writer.close();
		}





	}

	
	public void runTest(){
		//ASTUtils.getASTNode();
	}
	public void testStrategies(IPath currentProjectPath) throws IOException {
		IPath summaryPath = currentProjectPath.append("debug");

		try {

			SummaryStrategy<String> summary = new SummaryImpl1();
			testStrategy(summary, summaryPath);

			SummaryStrategy<IElement> edgeSummaryAll = new EdgeSummaryAll();
			testStrategy(edgeSummaryAll, summaryPath);
			SummaryStrategy<IElement> edgeSummaryAllX = new EdgeSummaryAllX();
			testStrategy(edgeSummaryAllX, summaryPath);

			SummaryStrategy<IElement> edgeSummaryIn = new EdgeSummaryIn();
			testStrategy(edgeSummaryIn, summaryPath);

			SummaryStrategy<IElement> edgeSummaryOut = new EdgeSummaryOut();
			testStrategy(edgeSummaryOut, summaryPath);

			SummaryStrategy<IElement> edgeSummaryInX = new EdgeSummaryInX();
			testStrategy(edgeSummaryInX, summaryPath);

			SummaryStrategy<IElement> edgeSummaryOutX = new EdgeSummaryOutX();
			testStrategy(edgeSummaryOutX, summaryPath);

			SummaryStrategy<IElement> edgeSummaryObjCreationCount = new EdgeSummaryObjCreationCount();
			testStrategy(edgeSummaryObjCreationCount, summaryPath);

			SummaryStrategy<IElement> edgeSummaryEdgeCount = new EdgeSummaryEdgeCount();
			testStrategy(edgeSummaryEdgeCount, summaryPath);
			
			SummaryStrategy<Integer> hierarchySummary = new HierarchySummary();
			hierarchySummary.compute();
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private <T> void printSet(Set<Info<T>> set, Writer out) throws IOException {
		if (set != null) {
			for (Info<T> s : set) {
				out.append(s.getKey() + " NUM[" + s.getNumber() + "]\n");

			}
		}
	}
	
	
	
	
}
