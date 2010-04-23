package femtoXML.app;

import femtoXML.FormatWriter;
import femtoXML.XMLCharUtil;

/**
 * Represents non-space, non-markup, XML content.
 * 
 * @author sufrin ($Revision$)
 * 
 */
public class AppContent extends AppTreeImp implements AppTree
{
  /** The text */
  protected String  text;

  /** Was the text a CDATA? */
  protected boolean cdata;

  /** Did the text have character entitities expanded within it? */
  protected boolean expandedEntities;

  /** Construct content */
  public AppContent(String text, boolean cdata, boolean expandedEntities)
  {
    this.text = text;
    this.cdata = cdata;
    this.expandedEntities = expandedEntities;
  }

  /** Construct a word (with <code>expandEntities</code> false) */
  public AppContent(String text, boolean cdata)
  {
    this(text, cdata, true);
  }

  /**
   * Construct a word (with <code>cdata</code> and <code>expandEntities</code>
   * false)
   */
  public AppContent(String text)
  {
    this(text, false, true);
  }

  /** The literal form of the stored text. */
  public String toString()
  {
    return text;
  }

  /**
	 * Outputs the text of this content (at the given indentation from the left
	 * margin, if indent>0).
	 * 
	 * <p>
	 * If this is CDATA content then the text is output literally as it was
	 * input (so that it can be reinput).
	 * </p>
	 * 
	 * <p>
	 * If <code>expandedEntities</code> is true, then it is output in valid
	 * XML form (so that it can be reinput) using <code>XMLCharUtil.print</code>
	 * (which substitutes for all character entities defined within
	 * <code>XMLCharUtil</code> if substitution is enabled for the
	 * <code>out</code> stream).
	 * </p>
	 * 
	 * <p>
	 * Otherwise it is output exactly as it was input.
	 * </p>
	 */
  public void printTo(FormatWriter out, int indent)
  {
    if (indent > 0) out.indent(indent);
    if (cdata)
    {
      out.print("<![CDATA[");
      out.print(text);
      out.println("]]>");
    }
    else
    {
      if (expandedEntities)
        XMLCharUtil.print(out, text);
      else
        out.print(text);
    }
  }

  public boolean isWord()
  {
    return !cdata;
  }

}
