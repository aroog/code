package edu.wayne.metrics;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.wayne.metrics.adb.CustomWriter;

public class CustomWriterTest {
	private CustomWriter writer;
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private String fileName;
	@Before
	public void setUp() throws Exception {
		fileName = "test/file.txt";
		writer = new CustomWriter(fileName);
		   System.setOut(new PrintStream(outContent));
		    System.setErr(new PrintStream(errContent));
	}

	@After
	public void tearDown() throws Exception {
	    System.setOut(null);
	    System.setErr(null);
	}

	@Test
	public void testWriteString() throws IOException {
		String test = null;
		writer.write(test);

		assertEquals("Writing null String to file:"+ fileName+ System.getProperty("line.separator") , errContent.toString());
	}

	@Test
	public void testAppendCharSequence() throws IOException {
		CharSequence test = null;
		writer.append(test);
		assertEquals("Writing null CharSequence to file:"+ fileName + System.getProperty("line.separator") , errContent.toString());
	}

}
