package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Represents a <code>DOCTYPE</code> declaration.
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
	 * Outputs the text of this PI.
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

}
