package femtoXML;

import java.io.PrintWriter;

public class AppWord implements AppTree
{
  protected String text;
  protected boolean cdata;

  public AppWord(String text, boolean cdata)
  {
    this.text = text;
    this.cdata = cdata;
  }
  
  /** Generates the human-readable form of the word text. */
  public String toString()
  {
    return text;
  }
  
  /** Outputs the text of this word at the given indentation in valid XML form (so that it can be reinput). 
   *  Non-ASCII characters are transformed into character entitities, unless this is a CDATA word.
   *  
   */
  public void printTo(PrintWriter out, int indent)
  {
    for (int i = 0; i < indent; i++)
      out.print(" ");
    if (cdata) 
    { out.print("<![CDATA[");
      out.print(text);
      out.print("]]>");
    }
    else
    for (int i=0; i<text.length(); i++)
    { 
      char c = text.charAt(i);
      if (cdata) out.print(c); else
      switch (c)
      {
        case '<':  out.print("&lt;"); break;
        case '>':  out.print("&gt;"); break;
        case '&':  out.print("&amp;"); break;
        case '"':  out.print("&quot;"); break;
        case '\'': out.print("&apos;"); break;
        default:   if (c>128) out.format("&#x%X;", (int) c); else out.print(c);
      }
    }
  }
}
