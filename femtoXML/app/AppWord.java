package femtoXML.app;

import java.io.PrintWriter;
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
  }
  
  public AppWord(String text, boolean cdata)
  {
    this.text = text;
    this.cdata = cdata;
    this.expandedEntities = true;
  }
  
  
  /** The literal form of the stored text. */
  public String toString()
  {
    return text;
  }
  
  /** Outputs the text of this word at the given indentation in valid XML form (so that it can be reinput). 
   *  Non-ASCII characters are transformed into character entitities, unless this is a CDATA word or 
   *  one for which entities have not been expanded on input.
   *  
   */
  public void printTo(PrintWriter out, int indent)
  {
    for (int i = 0; i < indent; i++) out.print(" ");
    if (cdata) 
    {  out.print("<![CDATA[");
       out.print(text);
       out.print("]]>");
    }
    else
    if (expandedEntities) 
       XMLCharUtil.print(out, text); 
    else 
       out.print(text);
  }
}
