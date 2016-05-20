package edu.wayne.metrics.mapping;

import java.util.Set;

import junit.framework.Assert;

//TOSUM: Do not modify
//TOMAR: do we really need to have a class so similar to ModelManager?
public class DefaultingModelManager {

	// Q: move singleton to the Model class? 
	private static DefaultingModelManager sinstance = null;

	private Model model = null;

	private DefaultingModelManager() {
	}

	public static DefaultingModelManager getInstance() {
		if (sinstance == null) {
			sinstance = new DefaultingModelManager();
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
		testGenerateXML("C:\\temp\\TestDefaultMap.xml");
		testLoadXML("C:\\temp\\TestDefaultMap.xml");
	}

	private static void testGenerateXML(String fileName) {
		DefaultingModel model = new DefaultingModel();
		model.addFrameworkPackage("cryptdob.core");

		DefaultingPersist.save(model, fileName);
		System.out.println("The XML file was generated.");
	}

	private static void testLoadXML(String fileName) {
		DefaultingModel model = DefaultingPersist.load(fileName);
		// The rest of the code should use the loaded object
		Set<String> fwkPackages = model.getFrameworkPackages();
		for(String fwkPackage : fwkPackages ) {
			Assert.assertEquals(fwkPackage, "cryptdob.core");
			break;
		}
		
		System.out.print(model.toString());
		System.out.println("The XML file was loaded.");
	}
}
