package secoog;

import java.lang.reflect.Field;

import secoog.itf.ISecElement;

public abstract class SecElement implements ISecElement{
	public String name;
	
	// DONE: Why just trustLevel? What about confidentialLevel? Why is this part of SElement? And the other properties are part of SComponent?
	// TODO: add more security properties here. 
	public  TrustLevelType trustLevel = TrustLevelType.Unknown;
	public  IsConfidential isConfidential = IsConfidential.Unknown;
	public  IsTransient isTransient = IsTransient.Unknown;
	public  IsEncrypted isEncrypted = IsEncrypted.Unknown;
	public  IsSerializable isSerializable = IsSerializable.Unknown;
	public  IsSanitized isSanitized = IsSanitized.Unknown;
	// TODO: XXX. convert into a set/array of properties
	// something like :
	//private Property[] properties = {trustLevel, isConfidential, isTransient, isEncrypted, isSerializable, isSanitized};

	// TODO: Maybe move this to an ISecVisitable interface; then having everything at least implement ISecVisitable (including SecGraph)
	public boolean accept(SecVisitor visitor) {
		return true;
	}

	@Override
	public boolean isEncrypted() {
		return isEncrypted.equals(IsEncrypted.True);
	}

	@Override
	public boolean isPartiallyTrusted() {
		return trustLevel.equals(TrustLevelType.Partial);
	}

	@Override
	public boolean isTrusted() {
		return trustLevel.equals(TrustLevelType.Full);
	}
	
	@Override
	public boolean isSerialized() {
		return isSerializable.equals(IsSerializable.True);
	}

	@Override
	public boolean isTransient() {
		return isTransient.equals(IsTransient.True);
	}
	
	@Override
	public boolean isSanitized() {
		return isSanitized.equals(IsSanitized.True);
	}
	
	@Override
	public boolean isConfidential() {
		return isConfidential.equals(IsConfidential.True);
	}
	
	public boolean hasPropertyValue(Property type) {

		Property propertyValue = getPropertyValue(type);
		if (propertyValue != null ) {
		    return propertyValue == type; 
		}
		else {
			
		// Figure out what the element is connected to, then try to compute a property value
		}

		// If all else fails, give up, produce a warning. Avoid silent failure.
		// Base case
		return false;
	}

	/*
	 * Using reflection to get the property
	 * make peace with the Devil until we cross the bridge...
	 * */
	public Property getPropertyValue(Property type) {
		try {			
			Field[] declaredFields = this.getClass().getFields();
			for (Field f:declaredFields){
				if (f.getType() == type.getClass())				
					return (Property)f.get(this);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	/*
	 * Using reflection to get the property
	 * */
	public void setPropertyValue(Property prop) {	
		try {			
			Field[] declaredFields = this.getClass().getFields();
			for (Field f:declaredFields){
				if (f.getType() == prop.getClass())				
					f.set(this, prop);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public Property getPropertyValue(String propName) {
		try {			
			Field[] declaredFields = this.getClass().getFields();
			for (Field f:declaredFields){
				if (f.getType().getSimpleName().compareTo(propName)==0)					
					return (Property)f.get(this);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}	
}
