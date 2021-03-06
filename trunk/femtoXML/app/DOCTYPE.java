package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Represents an <i>uninterpreted</i> <code>DOCTYPE</code> declaration.
 * 
 * @author sufrin
 * 
 */
public class DOCTYPE extends NodeImp implements Node
{
	protected String text;

	public DOCTYPE(String text)
	{
		this.text = text;
	}

	/** Generates the human-readable form of the word text. */
	public String toString()
	{
		return text;
	}

	/**
	 * Outputs the text of this DOCTYPE declaration.
	 * 
	 */
	public void printTo(FormatWriter out, int indent)
	{
		out.indent(indent);
		out.print("<!DOCTYPE ");
		out.print(text);
		out.println(">");
	}

	public boolean isWord()
	{
		return false;
	}

	public Node visit(Visitor v) {
		return v.visit(this);
	}
	
	public Node copy()
	{ return new DOCTYPE(text);
	}

}
