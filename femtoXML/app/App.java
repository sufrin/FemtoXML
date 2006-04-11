package femtoXML.app;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import femtoXML.FormatWriter;
import femtoXML.XMLParser;
import femtoXML.XMLScanner;
import femtoXML.XMLTreeFactory;
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
  { boolean expandEntities = true, isAscii=false;
    Vector<String> files = new Vector<String>();
    final Map<String,String> map = new HashMap<String,String>();
    final Set<String> spaces = new HashSet<String>();
    for (int i=0; i<args.length; i++)
    {   String arg = args[i];
        if (arg.equals("-p")) expandEntities = false; 
        else
        if (arg.equals("-h")) System.err.printf("-p -- don't expand entities inline%n-e key val -- expand &key; as val%n-a -- encode Unicode characters >= 128 as entities%n"); 
        else
        if (arg.equals("-a")) isAscii = true;
        else
        if (arg.equals("-e")) map.put(args[++i], args[++i]);
        else
        if (arg.equals("-s")) spaces.add(args[++i]);
        else
           files.add(arg);
    }
    
    XMLTreeFactory<AppTree> factory = new AppTreeFactory(expandEntities)
    {
      Pattern entity = 
        Pattern.compile("<!ENTITY[ ]+([A-Za-z0-9]+)[ ]+((\"([^\"]+)\")|(\'([^\']+)\'))[ ]*>", Pattern.MULTILINE);

      public AppTree newDOCTYPE(String data)
      {   int start = 0;
          Matcher m = entity.matcher(data);
          while (m.find(start))
          { String name = m.group(1);
            String value = m.group(4);
            if (value==null) value = m.group(6);
            map.put(name, value);
            // System.err.printf("DECLARE %s=%s%n", name, value);
            start = m.end();
          }
          
        return new AppDOCTYPE(data);
      }
    };
    
    XMLParser<AppTree> parser  = new XMLParser<AppTree>(factory)
    {
      public Reader decodeEntity(String name)
      {  String value = map.get(name);
         return new StringReader(value==null ? String.format("[[%s]]", name) : value);
      }
      
      public boolean wantSpaces(String elementKind)
      {
        return spaces.contains(elementKind);
      }
    };
    XMLScanner         scanner = new XMLScanner(parser);
    FormatWriter        out    = new FormatWriter(new OutputStreamWriter(System.out, "UTF-8"));
    scanner.setExpandEntities(expandEntities);
    out.setCharEntities(isAscii);
    for (String arg : files)
    { scanner.read(new LineNumberReader(new InputStreamReader(new FileInputStream(arg), "UTF-8")), arg);
      AppElement root = (AppElement) parser.getTree();
      for (AppTree tree : root) tree.printTo(out, 0);
      out.println();
      out.flush();
    }
  }
}
