package femtoXML.app;

import femtoXML.FormatWriter;
import femtoXML.XMLCharUtil;

/**
 * Represents a lump of unstructured text found within an XML element.
 * @author sufrin
 *
 */
public class AppContent implements AppTree
{ /** The text */
  protected String text;
  /** Was the text a CDATA? */
  protected boolean cdata;
  /** Did the text have character entitities expanded within it? */
  protected boolean expandedEntities;

  /** Construct content */
  public AppContent(String text, boolean cdata, boolean expandEntities)
  {
    this.text = text;
    this.cdata = cdata;
    this.expandedEntities = expandEntities;
  }
  
  /** Construct a word (with <code>expandEntities</code> false) */
  public AppContent(String text, boolean cdata)
  {
    this(text, cdata, true);
  }
  
  /** Construct a word (with <code>cdata</code> and <code>expandEntities</code> false) */
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
   * Outputs the text of this word (at the given indentation from the left
   * margin, if indent>0) in valid XML form (so that it can be reinput).
   * Non-ASCII characters are transformed into character entitities, unless this
   * is a CDATA word or one for which entities have not been expanded on input.
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
