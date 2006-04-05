package femtoXML;

import java.io.*;
/**
 * An exemplary femtoXML application that pretty-prints its input XML files onto the
 * standard output stream.
 * 
 * @author sufrin
 *
 */
public class App
{
  public static void main(String[] args) throws Exception
  {
    XMLParser<AppTree> parser  = new XMLParser<AppTree>(new AppTreeFactory())
    {
      public String decodeEntity(String name)
      {  if (name.equals("foo"))
            return "embedded&bar;stuff";
         else
         if (name.equals("bar"))
            return " bar ";
         else
            return name;
      }
    };
    XMLScanner         scanner = new XMLScanner(parser);
    PrintWriter        out     = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
    for (String arg : args)
    { scanner.read(new LineNumberReader(new InputStreamReader(new FileInputStream(arg), "UTF-8")), arg);
      AppElement root = (AppElement) parser.getTree();
      for (AppTree tree : root)
           tree.printTo(out, 0);
      out.println();
      out.flush();
    }
  }
}
