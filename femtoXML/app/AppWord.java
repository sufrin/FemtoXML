package femtoXML.app;

import femtoXML.FormatWriter;
import femtoXML.XMLCharUtil;

public class AppWord implements AppTree
{
  protected String text;
  protected boolean cdata, expandedEntities;

  public AppWord(String text, boolean cdata, boolean expandEntities)
  {
    this.text = text;
    this.cdata = cdata;
    this.expandedEntities = expandEntities;
    if (text.equals("")) System.err.println("EMPTY WORD");
  }
  
  public AppWord(String text, boolean cdata)
  {
    this(text, cdata, true);
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
