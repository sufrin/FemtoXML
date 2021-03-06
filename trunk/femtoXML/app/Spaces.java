package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Represents source space outside markup.
 * 
 * @author sufrin ($Revision$)
 * 
 */

public class Spaces extends NodeImp implements Node
{
	protected String text;

	public Spaces(String text)
	{
		this.text = text;
	}

	/** Generates the human-readable form of the word text. */
	public String toString()
	{
		return text;
	}

	/**
	 * Outputs the text of this space
	 * 
	 */
	public void printTo(FormatWriter out, int indent)
	{
		out.print(text);
	}

	public boolean isWord()
	{
		return false;
	}
	
	public Node visit(Visitor v) {
		return v.visit(this);
	}
	
	public Node copy() { return new Spaces(text); }

}
