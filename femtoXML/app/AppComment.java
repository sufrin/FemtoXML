package femtoXML.app;

import femtoXML.FormatWriter;


public class AppComment implements AppTree
{ protected String[] lines;
  public AppComment(String data)
  { lines = data.split("[ \\t]*\\n[ \\t]*");
  }
  
  public void printTo(FormatWriter out, int indent)
  { 
    out.indent(indent);
    if (lines.length==1) 
      out.println("<!--"+lines[0]+"-->");
    else
    {
      out.println("<!--");
      for (String line: lines)
      if (line.length()>0)
      {
        out.indent(indent+4);
        out.println(line);
      }
      out.indent(indent);
      out.println("-->");
    }
  }

  public boolean isWord()
  {
    return false;
  }
}

