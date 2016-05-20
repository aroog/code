package edu.wayne.metrics.mapping;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

// TOSUM: Do not modify
//TOMAR: do we really need to have a class so similar to Persist?
public class DefaultingPersist {

	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void save(DefaultingModel model, String path) {
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

	public static DefaultingModel load(String filename) {
		DefaultingModel read = null;
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File source = new File(filename);

		try {
			read = serializer.read(DefaultingModel.class, source);

			if (read != null ) {
				read.finish();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return read;
	}
}
