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
import femtoXML.XMLCharUtil;
import femtoXML.XMLInputReader;
import femtoXML.XMLParser;
import femtoXML.XMLScanner;
import femtoXML.XMLSyntaxError;
import femtoXML.XMLTreeFactory;
/**
 * An exemplary femtoXML application that pretty-prints its input XML files onto the
 * standard output stream.
 * 
 * @author sufrin 
 * @revision $Revision$
 *
 */
public class App
{
  public static void main(String[] args) throws Exception
  { App it = new App();
    it.run(args);
  }
  
  boolean literalOutput=false, 
           expandEntities=true, 
           isAscii=false, 
           wantDOCTYPE=true, 
           logDOCTYPE=false, 
           wantComment=true, 
           wantPI=true; 
    
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
                              "+D         -- LOG DOCTYPE DECLARATION DETAILS%n"+
                              "-c         -- ignore comments%n"+
                              "-i         -- indent the source text without expanding entities%n"+
                              "-x         -- do not re-encode characters in content on output (to simplify some markup tests)%n"+
                              ""); 
        else
        if (arg.equals("-a")) isAscii = true;
        else
        if (arg.equals("-i")) { expandEntities = false; literalOutput = true; }
        else
        if (arg.equals("-x")) { literalOutput = true; }
        else
        if (arg.equals("-e")) map.put(args[++i], args[++i]);
        else
        if (arg.equals("-s")) spaces.add(args[++i]);
        else
        if (arg.equals("-d")) wantDOCTYPE=false;
        else
        if (arg.equals("+D")) logDOCTYPE=true;
        else
        if (arg.equals("-c")) wantComment=false;
        else
        if (arg.equals("-p")) wantPI=false;
        else
           files.add(arg);
    }
    
    /**
     * A tree factory that processes entity declarations in DTDs. 
     * <b>WARNING:</b> the algorithm used here is ad-hoc and incomplete: whilst we expand
     * parameter entities in entity declarations, we do not treat any material outside
     * entity declarations.
     * 
     */
    XMLTreeFactory<AppTree> factory = new AppTreeFactory(expandEntities)
    { 
      Pattern entity = 
        Pattern.compile("<!ENTITY\\s+([%]{0,1})\\s*([A-Za-z0-9:_]+)\\s+(PUBLIC|SYSTEM|)\\s*((\"([^\"]*)\")|(\'([^\']*)\'))\\s*((\"([^\"]+)\")|(\'([^\']+)\'))?\\s*>", Pattern.MULTILINE);
      
      Pattern pref =
        Pattern.compile("%([A-Za-z0-9:_]+);", Pattern.MULTILINE);
      
      Pattern dtd =
        Pattern.compile("\\s*([A-Za-z0-9:_]+)\\s*((PUBLIC|SYSTEM)\\s*((\"([^\"]*)\")|(\'([^\']*)\'))\\s*((\"([^\"]+)\")|(\'([^\']+)\'))?)?\\s*(\\[(.*)\\])?\\s*", Pattern.DOTALL);
      
      
      // http://www.w3.org/TR/REC-xml/#sec-entexpand suggests that only &#...; are expanded during entity definition
      // (this doesn't seem completely right to me, but standards is standards!)
      Pattern cref = 
        Pattern.compile("&(#[A-Fa-f0-9:_]+);", Pattern.MULTILINE);
      
      final Map<String,String> pmap = new HashMap<String,String>();
      
      String cSubst(String value)
      { int           start = 0;
        Matcher       m = cref.matcher(value);
        StringBuilder b = new StringBuilder();
        while (m.find(start))
        { String pid = m.group(1);
          char c = XMLCharUtil.decodeCharEntity(pid);
          b.append(value.substring(start, m.start()));
          if (c=='\000') b.append("&"+pid+";"); else b.append(c);
          start=m.end();
        }
        b.append(value.substring(start));
        return b.toString();
      }
      
      String pSubst(String value)
      { int           start = 0;
        Matcher       m = pref.matcher(value);
        StringBuilder b = new StringBuilder();
        while (m.find(start))
        { String pid = m.group(1);
          String val = pmap.get(pid);
          if (val==null) val = "%"+pid+";";
          b.append(value.substring(start, m.start()));
          b.append(val);
          start=m.end();
        }
        b.append(value.substring(start));
        return b.toString();
      }
     
      @Override
      public AppContent newContent(String data, boolean cdata)
      {
        return new AppContent(data, cdata, !literalOutput);
      }
     
      @Override
      public AppTree newDOCTYPE(String data)
      {   Matcher d = dtd.matcher(data);
          if (d.lookingAt()) 
          { String nameDTD = d.group(1);
            String systemDTD = d.group(3);
            boolean isPublicDTD = "PUBLIC".equalsIgnoreCase(systemDTD);
            boolean isSystemDTD = "SYSTEM".equalsIgnoreCase(systemDTD) || isPublicDTD;
            String dtd1 = d.group(6);
            String dtd2 = d.group(11);
            if (dtd1==null) dtd1 = d.group(8);
            if (dtd2==null) dtd2 = d.group(13);
            System.err.printf("DTD %s %s %s %s%n", nameDTD, systemDTD, dtd1, dtd2);
          }
          else
            throw new XMLSyntaxError(getLocator(), "DOCTYPE declaration malformed");
          
          String internal = d.group(15);
          if (internal!=null) 
             processDTD(internal);
          return new AppDOCTYPE(data);
      }
      
      public void processDTD(String data)
      {
          int start = 0;
          Matcher m = entity.matcher(data);
          StringBuilder errors = new StringBuilder();
          start = 0;
          while (m.find(start))
          { boolean isPE     = m.group(1).equals("%");
            String   name     = m.group(2);
            String   system   = m.group(3);
            boolean isPublic = system.equalsIgnoreCase("PUBLIC");
            boolean isSystem = system.equalsIgnoreCase("SYSTEM") || isPublic;
            String   value    = m.group(6);
            String   value2   = m.group(11);
            if (value==null)  value  = m.group(8);
            if (value2==null) value2 = m.group(13);       
            // Sanity check
            if (!isPublic && value2!=null) errors.append(String.format("Malformed SYSTEM entity declaration %s%n", m.group(0)));
            if (isPublic  && value2==null) errors.append(String.format("Malformed PUBLIC entity declaration %s%n", m.group(0)));
            // For the moment we will only look at internal entities
            if (!isSystem)
            {  value = pSubst(cSubst(value)); // XML standard is weird
               (isPE ? pmap : map).put(name, value);
            }
            else
            {
               System.err.printf("Warning: [not yet implemented] %s%n", m.group(0));
            }
            if (logDOCTYPE)
               System.err.printf("ENTITY %s%s %s = %s [%s]%n",isPE?"%":"", system,  name, value, value2==null?"":value2);
            start = m.end();
          }
          if (errors.length()>0) throw new XMLSyntaxError(getLocator(), errors.toString()); 

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
      { scanner.read(new LineNumberReader(new XMLInputReader(new FileInputStream(arg))), arg);
        AppElement root = (AppElement) parser.getTree();
        for (AppTree tree : root) tree.printTo(out, 0);
        out.println();
        out.flush();
      }
      catch (XMLSyntaxError ex)
      {
        System.err.printf("%s%n", ex.getMessage());
      }
    }
  }
}
