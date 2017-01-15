package oogre.refinements.tac;

import oogre.analysis.Config;

public class SolutionManager {


	/**
	 * Handle the special case for Main class:
	 * if in Mainclass, cannot use owner (it's really shared) or p (Main does not take a param)
	 * 
	 * @param index
	 * @param refType
	 * @param isMain
	 * @return
	 */
	public static String getSolution(TMSolutionType index, RefinementType refType, boolean isMain) {
		if(isMain) {
			if (index.equals(TMSolutionType.P) || index.equals(TMSolutionType.OWNER) ) {
				return null;
			}
		}
		return getSolution(index, refType);
	}

	/**
	 * Return an <owner,alpha> pair.
	 * 
	 * Owner is implicitly specified by the refinement.
	 * Alpha is specified by the solutionType
	 * 
	 * @param index
	 * @param refType
	 * @return
	 */
	public static String getSolution(TMSolutionType index, RefinementType refType) {

		switch (refType) {
			case PushIntoPD:
				switch (index) {
					case OWNED:
						return "this.owned"; // <this.PD,this.owned>
					case PD: 
						return "this.PD"; // <this.PD,this.PD>
					case OWNER: 
						return "owner"; // <this.PD,owner>	
					case P: 
						return "p"; // <this.PD,p>
					case SHARED: 
						return "shared"; // <this.PD,shared>	
					default:
						throw new IllegalArgumentException();
				}
			case PushIntoOwned:
				switch (index) {
					case OWNED:
						return "this.owned"; // <this.owned,this.owned>
					case PD: 
						return "this.PD"; // <this.owned,this.PD>
					case OWNER: 
						return "owner"; // <this.owned,owner>	
					case P: 
						return "p"; // <this.owned,p>
					case SHARED: 
						return "shared"; // <this.owned,shared>	
					default:
						throw new IllegalArgumentException();
				}
			case PushIntoOwner:
				switch (index) {
					case OWNED:
						return "this.owned"; // <this.owned,this.owned>
					case PD: 
						return "this.PD"; // <this.owned,this.PD>
					case OWNER: 
						return "owner"; // <this.owned,owner>	
					case P: 
						return "p"; // <this.owned,p>
					case SHARED: 
						return "shared"; // <this.owned,shared>	
					default:
						throw new IllegalArgumentException();
				}
			case SplitUp:
				switch (index) {
					case OWNED:
						return "this.owned"; // <this.owned,this.owned>
					case PD: 
						return "this.PD"; // <this.owned,this.PD>
					case OWNER: 
						return "owner"; // <this.owned,owner>	
					case P: 
						return "p"; // <this.owned,p>
					case SHARED: 
						return "shared"; // <this.owned,shared>	
					default:
						throw new IllegalArgumentException();
				}
			default:
				throw new IllegalArgumentException();
			}

	}
}
