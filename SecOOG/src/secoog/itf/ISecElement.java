package secoog.itf;

import oog.itf.IElement;

public interface ISecElement extends IElement{

	public boolean isEncrypted();

	public boolean isPartiallyTrusted();

	public boolean isTrusted();

	public boolean isSerialized();

	public boolean isTransient();

	public boolean isSanitized();

	public boolean isConfidential();

}
