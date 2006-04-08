package femtoXML;

import java.io.*;
import java.util.Vector;
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
  { boolean expandEntities = false;
    Vector<String> files = new Vector<String>();
    for (String arg:args)
        if (arg.equals("-x")) expandEntities = true; 
        else
            files.add(arg);
    XMLParser<AppTree> parser  = new XMLParser<AppTree>(new AppTreeFactory(expandEntities))
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
    scanner.setExpandEntities(expandEntities);
    for (String arg : files)
    { scanner.read(new LineNumberReader(new InputStreamReader(new FileInputStream(arg), "UTF-8")), arg);
      AppElement root = (AppElement) parser.getTree();
      for (AppTree tree : root)
           tree.printTo(out, 0);
      out.println();
      out.flush();
    }
  }
}
