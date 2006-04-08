package femtoXML;

import java.io.PrintWriter;

public class AppComment implements AppTree
{ protected String data;
  public AppComment(String data) { this.data=data; }
  
  public void printTo(PrintWriter out, int indent)
  {
    for (int i = 0; i < indent; i++) out.print(" ");
    out.print("<!--");
    out.print(data.trim());
    out.println("-->");
  }
}
