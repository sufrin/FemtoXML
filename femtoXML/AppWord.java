package femtoXML;

public class AppWord implements AppTree
{
  protected String text;

  public AppWord(String text)
  {
    this.text = text;
  }

  public String toString()
  {
    return text;
  }

  public void printTo(java.io.PrintStream out, int indent)
  {
    for (int i = 0; i < indent; i++)
      out.print(" ");
    out.print(toString());
  }
}
