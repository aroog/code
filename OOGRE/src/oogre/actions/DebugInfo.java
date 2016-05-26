package oogre.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import oog.re.IHeuristic;
import oog.re.IOperation;
import oog.re.IRefinement;
import oog.re.RefinementState;
import oogre.refinements.tac.Heuristic;
import oogre.refinements.tac.Refinement;
import oogre.refinements.tac.TM;

import org.eclipse.core.runtime.Path;

/**
 *
 */
public class DebugInfo {

	private static final String HEADER = "RefId,Refinement,SourceType,DestinationDomain,DestinationType,State,#Implicits";
	
	private static final String SUMMARY_HEADER = "HeusPIO,HeusPIOCompleted,HeusPIP,HeusPIPCompleted,RefsPIO,RefsPIOCompleted,RefsPIP,RefsPIPCompleted,RefsSPU,RefsSPUCompleted";

	private static final String SEP = "*******************************************************";
	
	private CustomWriter writer = null;

	private String path;
	
	private List<Heuristic> heus;
	private List<Refinement> refs;
	
	public DebugInfo(String path, List<Heuristic> heus, List<Refinement> refs) {
		this.path = path;
		this.heus = heus;
		this.refs = refs;
	}
	
	// XXX. Extract constants
	private String getFileName() {
		StringBuilder builder = new StringBuilder();
		builder.append(path);
		// XXX. Maybe path an IPath object to get the correct concatenation
		builder.append(Path.SEPARATOR);
		builder.append("stats.csv");
		return builder.toString();
	}	
	
	public CustomWriter getWriter() {
		if (writer == null) {
			writer = new CustomWriter(getFileName());
		}
		return writer;
	}

	public void finish() {
		CustomWriter lWriter = getWriter();
		if (lWriter != null ) {
	
			//writeSeparator();
			writeHeader();
			writeHeuristics();
			
			writeHeader();
            writeRefinements();
			
			writeSummaryHeader();
			writeSummary();

			// XXX. NO need for diagnostics. 
			// writeSeparator();
			// writeDiagnostics();
			
			writeFooter();
			lWriter.close();
		}
	}


	private void writeSeparator() {
        writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.NEWLINE);
        writer.append(SEP);
        writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.NEWLINE);
    }

	private void writeDiagnostics() {
		int nDiscarded = 0;
		int nTMs = 0;

		int nInitialTFs = 0;
		int nCheckEqualityConstraints = 0;
		int nTypeCheck = 0;
		int nRespectPreviousRefs = 0;
		int nFindNextTM = 0;
		int nOther = 0;
			
		//List<TM> allTMs = TM.getAllTMs();
		List<TM> allTMs = new ArrayList<TM>();
		for(TM tm: allTMs) {
			nTMs++;
			
			if(tm.isDiscarded) {
				nDiscarded++;
				
				switch(tm.discardPhase) {
				case InitialRunTFs:
					nInitialTFs++;
				case CheckEqualityConstraints:
					nCheckEqualityConstraints++;
					break;
				case TypeCheck:
					nTypeCheck++;
					break;
				case RespectPreviousRefs:
					nRespectPreviousRefs++;
					break;
				case FindNextTM:
					nFindNextTM++;
					break;
				default:
					nOther++;
				}
			}
		}
		
		CustomWriter lWriter = getWriter();
		lWriter.append("Debugging...");
		lWriter.append(CSVConst.NEWLINE);		
		lWriter.append("nTMs");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nTMs);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nDiscarded");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nDiscarded);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nTFs");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nInitialTFs);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nCheckEqualityConstraints");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nCheckEqualityConstraints);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nTypeCheck");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nTypeCheck);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nRespectPreviousRefs");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nRespectPreviousRefs);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nFindNextTM");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nFindNextTM);
		lWriter.append(CSVConst.NEWLINE);
		lWriter.append("nOther");
		lWriter.append(CSVConst.COMMA);
		lWriter.append(nOther);
		lWriter.append(CSVConst.NEWLINE);		
	}


	private int nRef;
	private int nRefPIO;
	private int nRefPIP;
	private int nRefSPU;

	private int nRefCompleted;
	private int nRefUnsupported;
	private int nRefPIOCompleted;
	private int nRefPIPCompleted;
	private int nRefSPUCompleted;
	private int nRefPIOUnsupported;
	private int nRefPIPUnsupported;
	private int nRefSPUnsupported;
	
	private int nHeuPIO;
	private int nHeuPIP;
	
	private int nHeu;
	private int nHeuCompleted;
	private int nHeuPIOCompleted;
	private int nHeuPIPCompleted;
	
	private int nHeuUnsupported;
	private int nHeuPIOUnsupported;
	private int nHeuPIPUnsupported;
		
	private void writeDetails() {
		writeHeuristics();
		writeRefinements();
	}
	
	private void writeRefinements() {
		CustomWriter lWriter = getWriter();
		for(IRefinement ref : refs) {
			
			nRef++;
			
			String data = ((oogre.refinements.tac.BaseOperation)ref).getData();
			boolean pushIntoOwned = ref instanceof oogre.refinements.tac.PushIntoOwned;
			boolean pushIntoPD = ref instanceof oogre.refinements.tac.PushIntoPD;
			boolean splitUp = ref instanceof oogre.refinements.tac.SplitUp;
			if (pushIntoOwned) {
				nRefPIO++;
			}
			else if (pushIntoPD) {
				nRefPIP++;
			}
			else if (splitUp) {
				nRefSPU++;
			}
			
			// Generate ID
			lWriter.append(nRef);
        	lWriter.append(CSVConst.COMMA);
			lWriter.append(ref.getClass().getSimpleName());
        	lWriter.append(CSVConst.COMMA);
        	lWriter.append(ref.getSrcObject());
        	lWriter.append(CSVConst.COMMA);
        	lWriter.append(ref.getDomainName());
        	lWriter.append(CSVConst.COMMA);
        	lWriter.append(ref.getDstObject());
        	lWriter.append(CSVConst.COMMA);
			RefinementState state = ref.getState();
			switch (state) {
			case Completed:
				nRefCompleted++;
				nRefPIOCompleted += pushIntoOwned ? 1 : 0;
				nRefPIPCompleted += pushIntoPD ? 1 : 0;
				nRefSPUCompleted += splitUp ? 1: 0;
				break;
			case Unsupported:
				nRefUnsupported++;
				nRefPIOUnsupported += pushIntoOwned ? 1 : 0;
				nRefPIPUnsupported += pushIntoPD ? 1 : 0;
				nRefSPUnsupported += splitUp ? 1 : 0; 
				break;
			}
			// Implicit refs (ONLY for refinements, not heuristics!)
			if (ref instanceof Refinement ) {
				Collection<IOperation> implicits = ((Refinement)ref).getImplicits();
				lWriter.append(CSVConst.COMMA);
				// Just report the number of implicits
				// Not sure if we need to report more on them: #PIO, #PIP
				lWriter.append(implicits.size());
			}
			
			// Metrics data
			lWriter.append(state.toString());
			if(data != null ) {
				lWriter.append(CSVConst.COMMA);
				lWriter.append(data);
			}
        	lWriter.append(CSVConst.NEWLINE);
		}
	}
	
	private void writeHeuristics() {

		CustomWriter lWriter = getWriter();

		for(IHeuristic ref : heus) {
			nHeu++;
			// Generate ID
			lWriter.append(nHeu);
        	lWriter.append(CSVConst.COMMA);
			lWriter.append(ref.getClass().getSimpleName());
        	lWriter.append(CSVConst.COMMA);
			lWriter.append(ref.getSrcObject());
        	lWriter.append(CSVConst.COMMA);
        	String domainName = ref.getDomainName();
			lWriter.append(domainName);
			boolean pushIntoOwned = domainName.equals("owned");
			boolean pushIntoPD = domainName.equals("PD");
			if(pushIntoOwned) {
				nHeuPIO++;
			}
			else if(pushIntoPD) {
				nHeuPIP++;
			}
			// XXX. Param, split up
        	lWriter.append(CSVConst.COMMA);
        	lWriter.append(ref.getDstObject());
        	lWriter.append(CSVConst.COMMA);
        	lWriter.append(ref.getState().toString());
        	
			RefinementState state = ref.getState();
			switch (state) {
			case Completed:
				nHeuCompleted++;
				nHeuPIOCompleted += pushIntoOwned ? 1 : 0;
				nHeuPIPCompleted += pushIntoPD ? 1 : 0;
				break;
			case Unsupported:
				nHeuUnsupported++;
				nHeuPIOUnsupported += pushIntoOwned ? 1 : 0;
				nHeuPIPUnsupported += pushIntoPD ? 1 : 0;
				break;
			}
			// Metrics data
			String data = ((oogre.refinements.tac.BaseOperation)ref).getData();
			if(data != null ) {
				lWriter.append(CSVConst.COMMA);
				lWriter.append(data);
			}

        	lWriter.append(CSVConst.NEWLINE);
		}
    }
	
	private void writeSummary() {
		writer.append(this.nHeuPIO);
		writer.append(CSVConst.COMMA);
		writer.append(this.nHeuPIOCompleted);
		writer.append(CSVConst.COMMA);
		writer.append(this.nHeuPIP);
		writer.append(CSVConst.COMMA);
		writer.append(this.nHeuPIPCompleted);
		writer.append(CSVConst.COMMA);
		writer.append(this.nRefPIO);
		writer.append(CSVConst.COMMA);
		writer.append(this.nRefPIOCompleted);
		writer.append(CSVConst.COMMA);		
		writer.append(this.nRefPIP);
		writer.append(CSVConst.COMMA);
		writer.append(this.nRefPIPCompleted);
		writer.append(CSVConst.COMMA);
		writer.append(this.nRefSPU);
		writer.append(CSVConst.COMMA);
		writer.append(this.nRefSPUCompleted);
		
    }

	
	private void writeHeader() {
        // Add the header once
        writer.append(HEADER);
        writer.append(CSVConst.NEWLINE);
    }

	private void writeSummaryHeader() {
        // Add the header once
		writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.NEWLINE);
        writer.append(SUMMARY_HEADER);
        writer.append(CSVConst.NEWLINE);
    }

	private void writeFooter() {
		writer.append(CSVConst.NEWLINE);
	}
	
}
