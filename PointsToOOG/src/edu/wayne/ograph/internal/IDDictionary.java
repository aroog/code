package edu.wayne.ograph.internal;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.crystal.tac.model.TACInstruction;

public class IDDictionary {
	private static Set<String> ids = new HashSet<String>();
	private static long NUMBERID = 0;
	private static long UNIQUEID = 0;
		
	public static String generateID(String id) {
		if (!ids.contains(id) && !id.equals("")) {
			ids.add(id);
			return id;
		} else {
			StringBuilder buff = new StringBuilder();
			buff.append(id);
			buff.append(NUMBERID);
			NUMBERID++;
			ids.add(buff.toString());
			return buff.toString();
		}
	}

	/**
	 * generates a unique ID based on O_id and an a TAC expression
	 * */
	public static long generateMethodID(OObject o, TACInstruction invk){
		final long prime = 31;
		long result = 1;
		result = prime * result + ((o==null || o.getO_id() == null) ? 0 : o.getO_id().hashCode());
		result = prime * result + ((invk == null) ? 0 : invk.hashCode());
		return result;
		
	}

	public static String generateUniqueName() {
		// XXX. Ugly string concatenation!
		return Constants.UNIQUE+(UNIQUEID++);
	}
	
	
}
