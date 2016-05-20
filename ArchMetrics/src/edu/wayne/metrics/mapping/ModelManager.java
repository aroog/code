package edu.wayne.metrics.mapping;

import java.util.ArrayList;

import junit.framework.Assert;

//TOSUM: Do not modify
public class ModelManager {

	// Q: move singleton to the Model class? 
	private static ModelManager sinstance = null;

	private Model model = null;

	private ModelManager() {
	}

	public static ModelManager getInstance() {
		if (sinstance == null) {
			sinstance = new ModelManager();
		}
		return sinstance;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	// NOTE: This is just a unit test to illustrate the map
	public static void main(String[] args) {
		testGenerateXML("C:\\temp\\TestMap.xml");
		testLoadXML("C:\\temp\\TestMap.xml");
	}

	private static void testGenerateXML(String fileName) {
		Model model = new Model();

		model.addMapping(new Entry("java.awt.Dimension"));
		model.addMapping(new Entry("java.util.List"));
		model.addContainerType("java.util.HashMap");

		Persist.save(model, fileName);
		System.out.println("The XML file was generated.");
	}

	private static void testLoadXML(String fileName) {
		Model model = Persist.load(fileName);
		// The rest of the code should use the loaded object
		ArrayList<Entry> mappings = model.getMappings();
		if ( mappings.size() != 0 ) {
			Entry entry = mappings.get(0);
			Assert.assertEquals(entry.getType(), "java.awt.Dimension"); 
		}
		
		System.out.print(model.toString());
		System.out.println("The XML file was loaded.");
	}
}
