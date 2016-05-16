package ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;


public class TestUtils {

	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void save(BaseTraceability model, String path) {
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File result = new File(path);

		try {
			serializer.write(model, result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		compress(path);
	}

	public static BaseTraceability load(String filename) {
		BaseTraceability read = null;
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File source = new File(filename);

		try {
			read = serializer.read(BaseTraceability.class, source);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return read;
	}
	
	private static void compress(String path){
		String output = path+".zip";
		File file = new File(path);
		byte[] buffer = new byte[1024];
		try {
			FileOutputStream fos = new FileOutputStream(output);
			ZipOutputStream zipos = new ZipOutputStream(fos);
			ZipEntry entry = new ZipEntry(file.getName());
			FileInputStream in = new FileInputStream(path);
			zipos.putNextEntry(entry);
    		int len;
			while ((len = in.read(buffer)) > 0) {
    			zipos.write(buffer, 0, len);
    		}
    		in.close();
    		file.deleteOnExit();
    		zipos.closeEntry();
    		zipos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
