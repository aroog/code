package edu.wayne.ograph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//class AstTypeTransform implements Transform<ast.Type> {
//    @Override
//    public ast.Type read(String value) throws Exception {
//        return ast.Type.getUnknownType();
//    }
//    @Override
//    public String write(ast.Type value) throws Exception {
//        return value.toString();
//    }
//}
//
//class UnknownTypeTransform implements Transform<ast.Type.UnknownType> {
//    @Override
//    public ast.Type.UnknownType read(String value) throws Exception {
//        return (ast.Type.UnknownType)ast.Type.getUnknownType();
//    }
//    @Override
//    public String write(ast.Type.UnknownType value) throws Exception {
//        return value.toString();
//    }
//}
//
///**
// * Sample code to illustrate custom transform
// *
// */
//class MyMatcher implements Matcher {
//	
//    @Override
//    public Transform match(Class type) throws Exception {
//        if (type.equals(ast.Type.class))
//            return new AstTypeTransform();
////        else if (type.equals(ast.Type.UnknownType.class)) {
////        	return new UnknownTypeTransform();
////        }
//        return null;
//    }
//}


public class OOGUtils {
	/**
	 * Common method for creating the serializer object
	 * @return
	 */
	private static Serializer getSerializer() {
	    Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy/*, new MyMatcher()*/);
	    return serializer;
    }
	

	public static void saveJSON(OGraph model, String path) {
		ObjectMapper om = new ObjectMapper();
		
		File resultFile = new File(path);
		
		try {
			om.writeValue(resultFile, model);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        catch (IOException e) {
	        e.printStackTrace();
        }
	}
	
	public static OGraphState loadState(String path) {
		ObjectMapper om = new ObjectMapper();
		OGraphState readValue = null;

		File resultFile = new File(path);

		try {
			readValue = om.readValue(resultFile, OGraphState.class);
		}
		catch(JsonMappingException e) {
			// Will catch No content to map due to end-of-input
			e.printStackTrace();
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return readValue;
	}

	public static void saveState(OGraphState model, String path) {
		ObjectMapper om = new ObjectMapper();
		
		File resultFile = new File(path);
		
		try {
			om.writeValue(resultFile, model);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        catch (IOException e) {
	        e.printStackTrace();
        }
	}

	// TODO: Maybe return JSON string
	public static String saveJSON(OGraph model) {
		ObjectMapper om = new ObjectMapper();
		String data = null;
		
		try {
			data = om.writeValueAsString(model);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return data; 
	}
	
	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void save(OGraph model, String path) {
		Serializer serializer = getSerializer();
		
		File result = new File(path);

		try {
			serializer.write(model, result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void saveGZIP(OGraph model, String path) {
		Serializer serializer = getSerializer();
		
		File result = new File(path);
		
		try {
			OutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(result));
			serializer.write(model, gzipOutputStream);
			gzipOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OGraph load(String filename) {
		return load(filename, true);
	}
	
	public static OGraph load(String filename, boolean postProcess) {
		Serializer serializer = getSerializer();
		File source = new File(filename);

		OGraph read = null;
		try {
			read = serializer.read(OGraph.class, source);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if ( read != null ) {
			_postProcess(read);
		}
		
		return read;
	}

	public static OGraph loadGZIP(String filename) {
		return loadGZIP(filename, true);
	}
		
	public static OGraph loadGZIP(String filename, boolean postProcess) {
		Serializer serializer = getSerializer();

		OGraph read = null;
		try {
			InputStream gzipInputStream = new GZIPInputStream(new FileInputStream(filename));
			read = serializer.read(OGraph.class, gzipInputStream);
			gzipInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ( read != null ) {
			_postProcess(read);
		}

		return read;
	}
	
	private static void _postProcess(OGraph read) {
	    // Finish the OGraph, adjusting the backpointers
		FinishingVisitor finishingVisitor = new FinishingVisitor();
		read.accept(finishingVisitor);
		
		// Sanity check the OGraph
		SanityCheckVisitor sanityCheckingVisitor = new SanityCheckVisitor();
		read.accept(sanityCheckingVisitor);
    }
}
