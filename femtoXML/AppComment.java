package femtoXML;

import java.io.PrintWriter;

public class AppComment implements AppTree
{ protected String data;
  public AppComment(String data) { this.data=data; }
  
  public void printTo(PrintWriter out, int indent)
  { String [] lines = data.split("[ \\t]*\\n[ \\t]*");
    for (int i = 0; i < indent; i++) out.print(" ");
    if (lines.length==1) 
      out.println("<!--"+lines[0]+"-->");
    else
    {
      out.println("<!--");
      for (String line: lines)
      if (line.length()>0)
      {
        for (int i = 0; i < indent+4; i++) out.print(" ");
        out.println(line);
      }
      for (int i = 0; i < indent; i++) out.print(" ");
      out.println("-->");
    }
  }
}

