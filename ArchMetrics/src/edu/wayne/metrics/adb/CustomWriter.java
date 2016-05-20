package edu.wayne.metrics.adb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Custom Writer: flag cases where blank text is being written to CSV. If writing a blank/null string to CSV, replace it
 * with a non-blank string, e.g., "XXX", AND output a meaningful warning to Console Window.
 * 
 * TODO: Check empty strings
 * TODO: Check empty escaped/quoted string; it may not be legal for R either.
 */
public class CustomWriter extends FileWriter {
	private String fileName;
	public CustomWriter(File file) throws IOException {
	    super(file);
    }


	public CustomWriter(String fileName) throws IOException{
		super(fileName);
		this.fileName = fileName;
	}
	public CustomWriter(String fileName, boolean b) throws IOException {
		super(fileName, b);
	}

	@Override
	public void write(String str) throws IOException {
		if(str==null){
			str="XXX";
			if(fileName!=null){
				System.err.println("Writing null String to file:"+ fileName);
			}
		}
		super.write(str);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		
		if(csq==null){
			csq="XXX";
			if(fileName!=null){
				System.err.println("Writing null CharSequence to file:"+ fileName);
			}
		}
		return super.append(csq);
	}


	
	

}
