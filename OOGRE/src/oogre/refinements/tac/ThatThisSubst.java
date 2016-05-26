package oogre.refinements.tac;

// Introduce constants
public class ThatThisSubst {
	
	// Append this
	// TODO: Add sanity checks:
	// - do not qualify "shared"
	// - only "owned", "PD" or "any"
	// XXX. If the argument is always is constant, don't call it!
	public static String qualify(String domainName) {
		if(domainName.equals("shared") || domainName.equals("owner") || domainName.equals("p")){
			return domainName;
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append("this");
			builder.append(".");
			builder.append(domainName);
			return builder.toString();
		}
	}
	
	// XXX. DELETE. replace with qualify with "this" + subst. with "that"
	// XXX. If the argument is always is constant, don't call it!
	public static String qualifyWithThat(String domainName) {
		if(domainName.equals("shared") || domainName.equals("owner") || domainName.equals("p")){
			return domainName;
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append("that");
			builder.append(".");
			builder.append(domainName);
			return builder.toString();
		}
	}
	
	// Remove "this"
	// (for saving annotations)
	public static String unQualify(String domainName) {
		if(domainName != null && domainName.contains("this")){
			return domainName.substring("this.".length());
		}
		else{
			return domainName;
		}
	}

	// Perform: that/this
	// Performance. This method is used very heavily. Avoid using regular expression here, which will have to be compiled each time.
	public static String substThisWithThat(String domainName) {
		return org.apache.commons.lang3.StringUtils.replace(domainName, "this", "that");
	}

	// Perform: that/this
	// Performance. This method is used very heavily. Avoid using regular expression here, which will have to be compiled each time.
	public static String substThatWithRec(String domainName, String recvName) {
		return org.apache.commons.lang3.StringUtils.replace(domainName, "that", recvName);
	}
	
	// Perform: rcv/this
	// Performance. This method is used very heavily. Avoid using regular expression here, which will have to be compiled each time.
	public static String substThisWithRec(String domainName, String recvName) {
		return org.apache.commons.lang3.StringUtils.replace(domainName, "this", recvName);
	}

}
