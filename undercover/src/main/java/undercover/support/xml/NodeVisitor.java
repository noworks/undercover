package undercover.support.xml;

public interface NodeVisitor {
	void visitXmlDeclaration(XmlDeclaration node);
	void visitDoctypeDeclaration(DoctypeDeclaration node);
	void enterElement(Element node);
	void leaveElement(Element node);
	void visitText(Text node);
}
