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
import femtoXML.XMLSyntaxError;
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
  { App it = new App();
    it.run(args);
  }
  
  boolean expandEntities = true, isAscii=false, wantDOCTYPE=true, wantComment=true, wantPI=true; 
    
  public void run(String[] args) throws Exception
  {
    Vector<String> files = new Vector<String>();
    final Map<String,String> map = new HashMap<String,String>();
    final Set<String> spaces = new HashSet<String>();
    for (int i=0; i<args.length; i++)
    {   String arg = args[i];
        if (arg.equals("-h")) 
            System.err.printf("-p         -- ignore <? processing instructions%n"+
                              "-e key val -- expand &key; as val%n"+
                              "-a         -- encode Unicode characters >= 128 as entities%n"+
                              "-s tag     -- preserve spacing inside <tag markup%n"+
                              "-d         -- ignore <!DOCTYPE declarations%n"+
                              "-c         -- ignore comments%n"+
                              "-i         -- indent the source text without expanding entities%n"+
                              ""); 
        else
        if (arg.equals("-a")) isAscii = true;
        else
        if (arg.equals("-i")) expandEntities = false;
        else
        if (arg.equals("-e")) map.put(args[++i], args[++i]);
        else
        if (arg.equals("-s")) spaces.add(args[++i]);
        else
        if (arg.equals("-d")) wantDOCTYPE=false;
        else
        if (arg.equals("-c")) wantComment=false;
        else
        if (arg.equals("-p")) wantPI=false;
        else
           files.add(arg);
    }
    
    XMLTreeFactory<AppTree> factory = new AppTreeFactory(expandEntities)
    {
      Pattern entity = 
        Pattern.compile("<!ENTITY[ ]+([A-Za-z0-9]+)[ ]+((\"([^\"]+)\")|(\'([^\']+)\'))[ ]*>", Pattern.MULTILINE);
      
      @Override
      public AppTree newDOCTYPE(String data)
      {   int start = 0;
          Matcher m = entity.matcher(data);
          while (m.find(start))
          { String name = m.group(1);
            String value = m.group(4);
            if (value==null) value = m.group(6);
            map.put(name, value);
            //System.err.printf("DECLARE %s=%s%n", name, value);
            start = m.end();
          }
          
        return new AppDOCTYPE(data);
      }
      
      @Override      
      public boolean wantComment() { return wantComment; }     
      
      @Override      
      public boolean wantDOCTYPE() { return wantDOCTYPE; }
      
      @Override
      public boolean wantPI()      { return wantPI; }
          
    };
    
    XMLParser<AppTree> parser  = new XMLParser<AppTree>(factory)
    { @Override
      public Reader decodeEntity(String name)
      {  String value = map.get(name);
         return new StringReader(value==null ? String.format("&amp;%s;", name) : value);
      }
      
      @Override
      public boolean wantSpaces()
      {
        return stack.peek().wantSpaces();
      }     
    };
    
    XMLScanner         scanner = new XMLScanner(parser);
    FormatWriter        out    = new FormatWriter(new OutputStreamWriter(System.out, "UTF-8"));
    scanner.setExpandEntities(expandEntities);
    out.setCharEntities(isAscii);
    for (String arg : files)
    { try
      {
        scanner.read(new LineNumberReader(new InputStreamReader(new FileInputStream(arg), "UTF-8")), arg);
        AppElement root = (AppElement) parser.getTree();
        for (AppTree tree : root) tree.printTo(out, 0);
      }
      catch (XMLSyntaxError ex)
      {
        System.err.printf("%s%n", ex.getMessage());
      }
      out.println();
      out.flush();
    }
  }
}
