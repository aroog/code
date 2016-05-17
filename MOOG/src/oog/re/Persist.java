package oog.re;

import java.io.File;


import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

/**
 * XXX. Consolidate with heuristics.Persist
 *
 */
public class Persist {

	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void save(RefinementModel model, String path) {
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File result = new File(path);

		try {
			serializer.write(model, result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static RefinementModel load(String filename) {
		RefinementModel read = null;
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File source = new File(filename);

		try {
			read = serializer.read(RefinementModel.class, source);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return read;
	}
}
