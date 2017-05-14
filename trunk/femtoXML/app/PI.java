package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Represents a processing instruction. The entire text of the PI is
 * recorded.
 * 
 * @author sufrin ($Revision$)
 * 
 */
public class PI extends NodeImp implements Node
{
	protected String text;

	public PI(String text)
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
		out.print("<?");
		out.print(text);
		out.println("?>");
	}

	public boolean isWord()
	{
		return false;
	}
	
	public Node visit(Visitor v) {
		return v.visit(this);
	}
	
	public Node copy() { return new PI(text); }

}
