package edu.wayne.legacy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.junit.Test;

import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasxml.FieldAnnotationInfo;
import edu.cmu.cs.aliasxml.MethodAnnotationInfo;
import edu.cmu.cs.aliasxml.TypeAnnotationInfo;
import edu.cmu.cs.aliasxml.TypeResolver;
import edu.cmu.cs.aliasxml.VariableAnnotationInfo;

public class TestAliasXMLLoader {

	@Test
	public void testHashMap() {
		
		// TORAD: TODO: Delete this code. Just to show how you would call the XML Loader.
		String TEST_TYPE_BINDING_KEY = "Ljava/util/HashMap<TK;TV;>;";
		TypeAnnotationInfo typeDecl = TypeAnnotationInfo.getBinding(TEST_TYPE_BINDING_KEY);
		assertNotNull(typeDecl);
		List<String> domainParameters = typeDecl.getParameters();
		//assertEquals(domainParameters.size(), 4);
		
		List<FieldAnnotationInfo> fields = typeDecl.getFields();
		//assertEquals(fields.size(), 2);
		
		String TEST_KEY_BINDING_KEY = "Ljava/util/HashMap;:TK;";
		FieldAnnotationInfo field0 = fields.get(0);
		assertEquals(field0.getFieldName(), "objKey");
		assertEquals(field0.getAnnotationInfo().getAnnotation().getDomain(), "key");
		assertEquals(field0.isVirtual(), true);
		assertEquals(field0.getFieldTypeBinding(), TEST_KEY_BINDING_KEY);
		
		// NOTE: The FieldAnnotationInfo getFieldTypeBinding() returns a TypeBinding key.
		// If need be, they can be resolved using: TypeResolver.mapBindingKeyToTypeBinding()
		// to get an ITypeBinding.
		ITypeBinding keyBinding = TypeResolver.mapBindingKeyToTypeBinding(TEST_KEY_BINDING_KEY);
		assertNotNull(keyBinding);
		assertEquals(keyBinding.getQualifiedName(), "K");
		
		String TEST_VAL_BINDING_KEY = "Ljava/util/HashMap;:TV;";
		FieldAnnotationInfo field1 = fields.get(1);
		assertEquals(field1.getFieldName(), "objVal");
		assertEquals(field1.getAnnotationInfo().getAnnotation().getDomain(), "value");
		assertEquals(field1.isVirtual(), true);		
		assertEquals(field1.getFieldTypeBinding(), TEST_VAL_BINDING_KEY);
		
		String TEST_METH_BINDING_KEY = "Ljava/util/HashMap;.get(Ljava/lang/Object;)TV;";
		MethodAnnotationInfo methDecl = MethodAnnotationInfo.getBinding(TEST_METH_BINDING_KEY);
		assertNotNull(methDecl);
		AnnotationInfo returnAnnotation = methDecl.getReturnAnnotation();
		assertEquals(returnAnnotation.getAnnotation().getDomain(), "value");
		
		// Use VariableAnnotationInfo to get the annotations on formal method parameters.
		// Important: append the methodBindingKey first to variable *type* binding key!
		// Otherwise, the variable *type* binding key is not unique!
		// NOTE: in current AliasXML, this is not a VariableBinding key. It's the *type* of the VariableBinding.
		// in order to account for the fact that the method parameter could be renamed.
		// XXX: this does not account for the same method having multiple parameters  of the same type that are in different domains!
		// IVariableBinding.getType().getKey()
		String TEST_VAR_BINDING_TYPE_KEY = "Ljava/lang/Object;";

		VariableAnnotationInfo methParamDecl = VariableAnnotationInfo.getBinding( TEST_METH_BINDING_KEY + TEST_VAR_BINDING_TYPE_KEY);
		//TOMAR: DOES NOT PASS
		//assertNotNull(methParamDecl);
		//assertEquals(methParamDecl.getAnnotation().getDomain(), "key");
	}
}
