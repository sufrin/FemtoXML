package femtoXML.app;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import femtoXML.FormatWriter;
import femtoXML.XMLAttrMap;
import femtoXML.XMLAttributes;
import femtoXML.XMLInputReader;
import femtoXML.XMLParser;
import femtoXML.XMLScanner;
import femtoXML.XMLSyntaxError;
import static femtoXML.app.AppPred.*;

/**
 * Example of a <code>femtoXML</code> application. Its main useful function is
 * to pretty-print its input XML files onto the standard output stream, but it
 * can also transcode files, expand internal entities, etc. etc.
 * <p>
 * The femtoXML API has been simplified to the point where common tasks can be
 * accomplished straightforwardly. On the face of it this might be though to
 * compromise the versatility of the API, but this application demonstrates that
 * by appropriate use of inheritance one can achieve specialised effects. An
 * extreme example of this is the subclassing of the <code>AppTreeFactory</code>,
 * used below: it provides an <i>ad-hoc</i> means of analysis of DTDs that
 * supports the definition of internal entities. Note the sharing of
 * <code>map</code> between the tree factory, the command-line interpreter,
 * and the <code>XMLParser.decodeEntity</code> method of the parser that is
 * passed to the <code>XMLScanner</code> constructed later.
 * </p>
 * 
 * @author sufrin ($Revision$)
 * 
 */
public class App
{
  public static void main(String[] args) throws Exception
  { App it = new App();
    it.run(args);
  }
  
  /** Command-line switch state*/
  boolean  literalOutput=false, 
           expandEntities=true, 
           isAscii=false, 
           wantComment=true, 
           wantPI=true,
           wantENC=false,
           alignParam = true,
           testPath = false; 
  
  /** Command-line switch state*/
  String enc = "UTF-8", ienc = null;
  
  /** Command-line switch state*/
  int splitParam = 2;
  
   
  XMLTreeFactoryWithDTDHandling factory = new XMLTreeFactoryWithDTDHandling(expandEntities);
 
  /** Mapping from internal entity names to their expansions */
  final Map<String,String> map = factory.getMap();
  
  /** Tags of the elements that require spaces to be preserved */
  final Set<String>        spaces = new HashSet<String>();


  /**
   * An <code>XMLparser</code> that implements (internal) entity decoding by using the
   * <code>map</code>; forwards <code>wantSpaces()</code> to the currently-open element; 
   * and plugs appropriate parameter values into <code>XMLAttributes</code>
   * as they are constructed.
   */
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
    
    @Override
    public XMLAttributes newAttributes(boolean expandEntitites)
    { XMLAttrMap  map = new XMLAttrMap()
                               .setExpandedEntities(expandEntitites)
                           .setSplit(splitParam)
                           .setAlign(alignParam);
      return map;
    }

  };
  
   
  public void run(String[] args) throws Exception
  {
    Vector<String> files = new Vector<String>();
    for (int i=0; i<args.length; i++)
    {   String arg = args[i];
        if (arg.startsWith("-h")) 
            System.err.printf("-p         -- ignore <? processing instructions%n"+
                              "-e key val -- expand &key; as val%n"+
                              "-a         -- encode Unicode characters >= 128 as entities%n"+
                              "-s tag     -- preserve spacing inside <tag markup%n"+
                              "-d         -- ignore <!DOCTYPE declarations%n"+
                              "+D         -- LOG DOCTYPE DECLARATION DETAILS%n"+
                              "-c         -- ignore comments%n"+
                              "-i         -- indent the source text without expanding entities%n"+
                              "-x         -- do not re-encode characters in content on output (to simplify some markup tests)%n"+
                              "-enc  enc  -- output encoding is enc (default is UTF-8)%n"+
                              "-ienc enc  -- input encoding is enc (the program deduces the encoding otherwise)%n"+
                              "-aa        -- don't bother aligning attribute values in tags%n"+
                              "-as <int>  -- show attributes on separate lines of there are more than <int> of them (default 2)%n"+
                              "-TP        -- test the path iterators features%n"+
                              "($Revision$)%n"); 
        else
        if (arg.equals("-a"))   isAscii = true;
        else
        if (arg.equals("-aa"))   alignParam = false;
        else
        if (arg.equals("-as"))   splitParam = Integer.parseInt(args[++i]);
        else
        if (arg.equals("-TP"))   testPath = true;
        else
        if (arg.equals("-i"))   { expandEntities = false; factory.setLiteralOutput(true); }
        else
        if (arg.equals("-x"))   { factory.setLiteralOutput(true); }
        else
        if (arg.equals("-enc")) { enc=args[++i]; wantENC = true; }
        else
        if (arg.equals("-ienc"))ienc=args[++i]; 
        else
        if (arg.equals("-e"))   map.put(args[++i], args[++i]);
        else
        if (arg.equals("-s"))   spaces.add(args[++i]);
        else
        if (arg.equals("-d"))   factory.setWantDOCTYPE(false);
        else
        if (arg.equals("+D"))   factory.setLogDOCTYPE(true);
        else
        if (arg.startsWith("-D"))
        { String [] argt = arg.substring(2).split("=", 2);
          if (argt.length==2) System.setProperty(argt[0], argt[1]);  else System.err.println(arg+"?");        
        }
        else
        if (arg.equals("-c"))   factory.setWantComment(false);
        else
        if (arg.equals("-p"))   factory.setWantPI(false);
        else
           files.add(arg);
    }
    

    XMLScanner         scanner = new XMLScanner(parser);
    FormatWriter       out     = new FormatWriter(new OutputStreamWriter(System.out, enc));
    scanner.setExpandEntities(expandEntities);
    out.setCharEntities(isAscii);
    for (String arg : files)
    { try
      { 
        scanner.read(new LineNumberReader(new XMLInputReader(new FileInputStream(arg), ienc)), arg);
               
        if (wantENC)
        {
          out.println(String.format("<?xml version='1.1' encoding='%s'?>%n", enc));
        }
        AppElement root = (AppElement) parser.getTree();
        if (testPath)
           testPathFeatures(root);
        else
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
  
  /////////////////////////// PATH FEATURES TESTBED /////////////////////////
  
     
  /*
     Various traversals
  */ 
  public void testPathFeatures(AppTree t) throws UnsupportedEncodingException
  {      FormatWriter       out     = new FormatWriter(new OutputStreamWriter(System.out, enc));
         Pred.Cached<AppTree> pred = isElement().and(below(isElementMatching("col"), 1)).cache();
         Pred.Cached<AppTree> cont = isElement().and(below(isElementMatching("col"), 1).and(containing(isElementMatching(".*tt.*")))).cache();
         // Statistics for the caching: count the nodes
         long nodes = 0;
         for (AppTree node : t.prefixIterator()) nodes++;
         
         // containment
         for (AppTree node : t.prefixIterator().filter(cont)) { node.printTo(out, 0); out.println(); }
         out.flush();
         System.err.printf("Without cutoff: nodes: %d; inspected: %d; missed %d%n", nodes, cont.hits,  cont.cachemisses);
         
         for (AppTree node : t.prefixIterator().filter(pred)) { node.printTo(out, 0); out.println(); }
         out.flush();
         System.err.printf("Without cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n", nodes, pred.hits,  pred.cachemisses);
         
         pred.hits=pred.cachemisses=0;
         for (AppTree node : t.prefixIterator(pred).filter(pred)) { node.printTo(out, 0); out.println(); }
         out.flush();
         // Statistics for the cacheing
         System.err.printf("With cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n", nodes, pred.hits,  pred.cachemisses);
         
         pred.hits=pred.cachemisses=0;
         // filtered without cutoff
         for (AppTree node : t.breadthIterator().filter(pred)) { node.printTo(out, 0); out.println(); }
         out.flush();
         System.err.printf("Without cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n", nodes, pred.hits,  pred.cachemisses);
         
         pred.hits=pred.cachemisses=0;
         for (AppTree node : t.breadthIterator(pred).filter(pred)) { node.printTo(out, 0); out.println(); }
         out.flush();
         // Statistics for the cacheing
         System.err.printf("With cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n", nodes, pred.hits,  pred.cachemisses);
  }
  
  public void testPathFeaturesBasic(AppTree t)
  { testRecursive(t);
    System.out.println("-------------------------");
    for (AppTree node : t.prefixIterator())
    {
        System.out.printf("%20s    ", (node.isElement() ? "<"+((AppElement) node).getKind() : node.toString()));
        for (AppTree s: t.pathToRoot()) System.out.print(s.elementName()+"/");
        System.out.println();       
    }
    System.out.println("-------------------------");
    for (AppTree node : t.breadthIterator())
    {
        System.out.printf("%20s    ", (node.isElement() ? "<"+((AppElement) node).getKind() : node.toString()));
        for (AppTree s: node.pathToRoot()) System.out.print(s.elementName()+"/");
        System.out.println();       
    }
  }
  
  public void testRecursive(AppTree t)
  { 
    if (t.isElement())
    {
       for (AppTree s: t.pathToRoot()) System.out.print(s.elementName()+"/");
           System.out.println();       
       for (AppTree subtree: t) testRecursive(subtree);
    }    
    
  }
  
  
}





