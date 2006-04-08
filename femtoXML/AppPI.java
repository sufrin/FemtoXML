package femtoXML;

import java.io.PrintWriter;

public class AppPI implements AppTree
{
  protected String text;

  public AppPI(String text)
  {
    this.text = text;
  }
  
  
  /** Generates the human-readable form of the word text. */
  public String toString()
  {
    return text;
  }
  
  /** Outputs the text of this PI. 
   *  
   */
  public void printTo(PrintWriter out, int indent)
  {
    for (int i = 0; i < indent; i++)
      out.print(" ");
      out.print("<?");
      out.print(text);
      out.println("?>");
  }
}

