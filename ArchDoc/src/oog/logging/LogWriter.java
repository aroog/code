package oog.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.Type;
import ast.TypeInfo;
import edu.wayne.summary.Crystal;
import edu.wayne.summary.strategies.InfoIElement;

/**
 * Keep a log of ArchDoc's operations
 * 
 * @author Yibin Wang
 * 
 * @ClassName: full name of class
 * @Methodname: can be empty
 * @FieldName: can be empty
 * @ClassType: class or interface
 * @OrderNum: track the sequence of process, each number represents an operation
 * @Mark: JRipples View Mark. In ArchDoc, empty
 * @Rank: probability. In ArchDoc, this is an int value
 * @Reason: comments by user
 * @ARS: a metrics. In ArchDoc, it's the number of classes in MIRC after filtering out library classes
 * @NewTypes: namelist of NEW non-library classes/interfaces added, exists through all periods of one Incremental Change
 * @NewTypesNum: the number of NewTypes
 * Attention: DRT isn't equal to the sum of NewTypesNum because NewTypes doesn't including the opened type itself.
 * @AllTypes: a metrics, record ALL subclasses that implements the interface and computed only if the interface is visited
 * @AllTypesNum: the number of sub-classes
 * @MCBI: In ArchDoc, record ALL contents in MCBI view. In JRipples, empty.
 * @MCBINum: the number of MCBI
 * @Period: concept location/impact analysis/change propagation. In ArchDoc, CL & IA.
 */
public class LogWriter {
	// save the log in dir
	private static String dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	//"C:\\Users\\Administrator\\Desktop";

	// log related
	public static int LogState = 0;
	public static int OrderNum = 0;
	public static String Reason = "";
	public static boolean CmmLock = false;
	public static HashSet<String> newTypes = new HashSet<String>();
	public static HashSet<String> CheckedTypes = new HashSet<String>();
	public static HashSet<String> CheckedInterfaces = new HashSet<String>();
	
	private static HashSet<String> projectTypes = new HashSet<String>();
	
	/**
	 * Record MCBI for ONE line of log in AllTypes & AllTypesNum columns. This time generate
       the selected field name, Rank is still 0, Order keeps the same, ARS equals AllTypesNum
	 * @param type
	 * @param elements
	 */
	public static void setLogForDelaration(ITypeBinding type, String variableName, String path, Set elements) {
		int NewTypeNum = 0;
		String NewType = "";
		String ClassType = "";
		String AllTypes = "";
		int AllTypesNum = 0;
		if (type.isInterface()){
			ClassType = "INTERFACE";
		}
		else{
			ClassType = "CLASS";
		}
		//compute AllTypesNum and AllTypes		
		TypeInfo typeInfo = TypeInfo.getInstance();
		Type typeAST = typeInfo.getType(path);
		if (typeAST != null) {
			// NOTE: Use Set to hash out duplicates.
			Set<String> allSubClasses = new HashSet<String>();
			
		    //compute AllTypesNum and AllTypes		
			// Compute transitive subclasses.
			if (typeAST != null) {
				getSubClasses(typeAST, allSubClasses);
			}
			
			StringBuilder  builder = new StringBuilder();
			for(String all: allSubClasses ) {
				builder.append(all);
				builder.append(",");
			}
			
			AllTypesNum = allSubClasses.size();
			AllTypes = builder.toString();
		}

		int Rank = 0;
		int ARS = AllTypesNum;//this time AllTypesNum is NOT consistent with elements in adjacentTypes
		String timeStamp = getTime();
		String MCBI = "";
		int MCBINum = 0;

		ArrayList<InfoIElement> adjacentTypes = new ArrayList<InfoIElement>();
		ArrayList<Integer> adjacentRanks = new ArrayList<Integer>();
		int i = 1;
		if (elements!=null){
			for (Iterator iter = elements.iterator(); iter.hasNext(); i++) {
				InfoIElement element = (InfoIElement) iter.next();
				MCBINum++;
				MCBI += element.getKey() + ", ";
				if (!isFromLibrary(element)) {
					adjacentTypes.add(element);
					adjacentRanks.add(i);
				}
			}
			//get NewTypeNum and NewType:
			//usually results in MCBI are already contained in MIRC, but some may not;
			//this one also handles with the condition that see MCBI before MIRC when open a new type
			if (adjacentTypes.size() != 0)
				for (int j = 0; j < adjacentTypes.size(); j++) {
					if (newTypes.add(adjacentTypes.get(j).getKey().toString())) {
						NewTypeNum++;
						NewType += adjacentTypes.get(j).getKey().toString() + ", ";
					}
				}
			ARS = adjacentTypes.size();
		}

		// write a log for current declaration
		writeALineInCSV(type.getQualifiedName(), "", variableName, ClassType, "",
				Rank, ARS, NewTypeNum, NewType, AllTypes, AllTypesNum, MCBI, MCBINum, timeStamp, Reason);
		// write logs for classes in MCBI
		if (adjacentTypes.size() != 0) {
			for (int j = 0; j < adjacentTypes.size(); j++)
				writeALineInCSV(adjacentTypes.get(j).getKey(), "", "",
						adjacentTypes.get(j).getType().toString(), "",
						(int) adjacentRanks.get(j), 0, 0, "", "", 0, "", 0, timeStamp,"");
		}
	}
	
	
	// Build list of subclasses that TypeA can refer to, *transitively* by calling this recursively
    private static void getSubClasses(Type type, Set<String> allSubClasses) {
    	// append the current type.
    	allSubClasses.add(type.toString());
    	
    	for (Type subType : type.getSubClasses()) {
    		getSubClasses(subType, allSubClasses);
    	}
    }

	public static void setLogs(ITypeBinding type, Set elements, String mark) {
		String ClassType = "";
		String AllTypes = "";
		int AllTypesNum = 0;
		if (type.isInterface()){
			ClassType = "INTERFACE";
		}
		else{
			ClassType = "CLASS";
		}

		int Rank = 0;
		int ARS = 0;
		int NewTypeNum = 0;
		String NewType = "";
		String timeStamp = getTime();
		ArrayList<InfoIElement> adjacentTypes = new ArrayList<InfoIElement>();
		ArrayList<Integer> adjacentRanks = new ArrayList<Integer>();
		int i = 1;

		if (elements!=null) {
			for (Iterator iter = elements.iterator(); iter.hasNext(); i++) {
				InfoIElement element = (InfoIElement) iter.next();
				// System.out.println(element.getNumber());
				if (!isFromLibrary(element)) {
					adjacentTypes.add(element);
					adjacentRanks.add(i);
				}
			}
			//ARS = i - 1; //if include library types
			ARS = adjacentTypes.size(); //if filter out library types
			//get NewTypeNum and NewType
			if (adjacentTypes.size() != 0) {
				for (int j = 0; j < adjacentTypes.size(); j++) {
					if (newTypes.add(adjacentTypes.get(j).getKey().toString())) {
						NewTypeNum++;
						NewType += adjacentTypes.get(j).getKey().toString() + ", ";
					}
				}
			}
		}

		// write a log for current type
		writeALineInCSV(type.getQualifiedName(), "", "", ClassType, mark,
				Rank, ARS, NewTypeNum, NewType, AllTypes, AllTypesNum, "", 0, timeStamp, Reason);
		// write logs for classes in MIRC
		if (adjacentTypes.size() != 0) {
			for (int j = 0; j < adjacentTypes.size(); j++)
				writeALineInCSV(adjacentTypes.get(j).getKey(), "", "",
						adjacentTypes.get(j).getType().toString(), "",
						(int) adjacentRanks.get(j), 0, 0, "", "", 0, "", 0, timeStamp, "");
		}
	}

	/**
	 * Filter out types from libraries
	 * @param element
	 * @return true if element.getKey() is a type from libraries
	 */
	public static boolean isFromLibrary(InfoIElement element) {
		// ITypeBinding typeBinding = Crystal.getInstance().getTypeBindingFromName(element.getKey());
		String typeName = element.getKey();
		/*
		String[] names = typeName.split("\\.");
		String packName = "";
		for (int i = 0; i < names.length - 1; i++) {
			packName += names[i] + ".";
		}*/
		if(projectTypes.contains(typeName))
			return false;
		else
			return true;
	}
	
	public static boolean isEmptyProjectTypes(){
		return projectTypes.isEmpty();
	}
	
	/**
	 * get all types of current project 
	 */
	public static void initProjectTypes() {
		//This time, the way to get all types of a project is correct
		//TODO: may extend to get all inner types if possible and necessary in future study
		Crystal instance = Crystal.getInstance();
		Iterator<ICompilationUnit> compilationUnitIterator = instance
				.getCompilationUnitIterator();
		if (compilationUnitIterator != null) {
			while (compilationUnitIterator.hasNext()) {
				ICompilationUnit cu = compilationUnitIterator.next();
				try {
					for (IType type : cu.getTypes()) {
						String key = type.getFullyQualifiedName();
						// System.out.println(key);
						projectTypes.add(key);
					}
				} catch (JavaModelException e) {
					throw new RuntimeException(
							"Unexpected problem: No nodes in " + instance);
				}
			}
		}
		/*
		IWorkbenchWindow window = LoadUtils.getWindow();
		if(window!=null){
			IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
			if (currentProject != null) {
				try {
					IPackageFragment[] myPackages = JavaCore.create(currentProject).getPackageFragments();
					for (IPackageFragment myPackage : myPackages) {
						for (ICompilationUnit unit : myPackage.getCompilationUnits()) {
							IPackageDeclaration[] pck = unit.getPackageDeclarations();
							for (IPackageDeclaration pack : pck) {
								String packName = pack.getElementName();
								projectPacks.add(packName+".");
							}
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}*/
	}
	
	public static void updateProjectTypes() {
		//TODO: use this method if needed
		//at this moment, it is useless
		initProjectTypes();
	}

	private static String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String timeStamp = df.format(new Date()).toString();
		return timeStamp;
	}

	private static void writeALineInCSV(String ClassName, String Methodname,
			String FieldName, String ClassType, String Mark, int Rank, int ARS,
			int NewTypeNum, String NewType, String AllTypes, int AllTypesNum,
			String MCBI, int MCBINum, String timeStamp, String Comment) {
		try {
			// System.out.println(dir);
			File F0 = new File(dir);
			File F1 = new File(dir + "\\log.csv");
			// create the log file if not exist
			if (!F1.exists()) {
				if (!F0.mkdir()) {
					System.out.println("Fail to create the directory");
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(dir
						+ "\\log.csv", true));
				out.write("ClassName,MethodName,FieldName(ParamsName),ClassType,Period,Mark,Order,Rank,Comment,ARS,"
						+ "NewTypesNum,NewTypes,AllTypes,AllTypesNum,MCBINum,MCBI,TimeStamp");
				out.newLine();
				out.close();
			}
			String Period = "CL&IA";

			BufferedWriter out = new BufferedWriter(new FileWriter(dir
					+ "\\log.csv", true));
			// add "" around Comment in case containing "," in this value
			// same for all long Strings
			out.write("\"" + ClassName + "\",\"" + Methodname + "\",\"" + FieldName + "\","
					+ ClassType + "," + Period + "," + Mark + "," + OrderNum
					+ "," + Integer.toString(Rank) + ",\"" + Comment + "\"," + ARS + ","
					+ NewTypeNum + ",\"" + NewType + "\",\"" + AllTypes + "\","
					+ AllTypesNum + "," + MCBINum + ",\"" + MCBI + "\"," + timeStamp);
			out.newLine();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeEmptyLines(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dir + "\\log.csv", true));
			out.newLine();
			out.newLine();
			out.newLine();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
