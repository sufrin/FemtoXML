package femtoXML.app;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import femtoXML.XMLParser;
import femtoXML.XMLScanner;
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
  { boolean expandEntities = true;
    Vector<String> files = new Vector<String>();
    final Map<String,String> map = new HashMap<String,String>();
    for (int i=0; i<args.length; i++)
    {   String arg = args[i];
        if (arg.equals("-p")) expandEntities = false; 
        else
        if (arg.equals("-h")) System.err.printf("-p -- don't expand entities inline%n-e key val -- expand &key; as val%n"); 
        else
        if (arg.equals("-e")) map.put(args[++i], args[++i]);
        else
            files.add(arg);
    }
    XMLParser<AppTree> parser  = new XMLParser<AppTree>(new AppTreeFactory(expandEntities))
    {
      public Reader decodeEntity(String name)
      {  String value = map.get(name);
         return new StringReader(value==null ? name : value);
      }
    };
    XMLScanner         scanner = new XMLScanner(parser);
    PrintWriter        out     = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"));
    scanner.setExpandEntities(expandEntities);
    for (String arg : files)
    { scanner.read(new LineNumberReader(new InputStreamReader(new FileInputStream(arg), "UTF-8")), arg);
      AppElement root = (AppElement) parser.getTree();
      for (AppTree tree : root) tree.printTo(out, 0);
      out.println();
      out.flush();
    }
  }
}
