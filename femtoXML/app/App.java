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
  
  /** Returns an iterator that yields the path back to the root (as Trees) */
  
  public AppIterator<AppTree> toRoot(final AppTree here)
  { return new AppIterator<AppTree>()
    { AppTree cursor = here;
      public boolean hasNext()
      { return cursor !=null; 
      } 
      public AppTree next()
      { AppTree result = cursor;
        cursor = cursor.getParent();
        return result;
      }
    };
  }
  
  /** Returns an iterator that yields the path back to the root (as the names of elements) 
      with "-" for a leaf.
  */
  public AppIterator<String> pathToRoot(final AppTree here)
  { return new AppIterator<String>()
    { AppTree cursor = here;
      public boolean hasNext()
      { return cursor !=null; 
      } 
      public String next()
      { String result = 
         (cursor instanceof AppElement) ? ((AppElement) cursor).getKind() : "-";
        cursor = cursor.getParent();
        return result;
      }
    };
  }
  
  /** Returns a prefix order depth-first iterator */ 
  public AppIterator<AppTree> prefixIterator(final AppTree here)
  { return new AppIterator<AppTree>()
    { /** Acts as a stack */
      AppIterator<AppTree> agenda = new AppIterator.Unit<AppTree>(here);
      
      public boolean hasNext()
      { return agenda.hasNext(); }
      
      public AppTree next()
      { assert(hasNext());
        AppTree result = agenda.next();
        // Push the subtrees onto the stack
        if (result.isElement())
           agenda = new AppIterator.Cat<AppTree>(result.iterator(), agenda);
        return result;
      }
    };
  }
  
  /** Returns a breadth-first order iterator */  
  public AppIterator<AppTree> breadthIterator(final AppTree here)
  { return new AppIterator<AppTree>()
    { /** Acts as a queue */
      AppIterator<AppTree> agenda = new AppIterator.Unit<AppTree>(here);
      
      public boolean hasNext()
      { return agenda.hasNext(); }
      
      public AppTree next()
      { assert(hasNext());
        AppTree result = agenda.next();
        // Queue the subtrees 
        if (result.isElement())
           agenda = new AppIterator.Cat<AppTree>(agenda, result.iterator());
        return result;
      }
    };
  }
  
  /*
     Various traversals -- showing paths back to the root.
  */
  public void testPathFeatures(AppTree t)
  { testRecursive(t);
    System.out.println("-------------------------");
    for (AppTree node : prefixIterator(t))
    {
        System.out.printf("%20s    ", (node.isElement() ? "<"+((AppElement) node).getKind() : node.toString()));
        for (String s: pathToRoot(node)) System.out.print(s+"/");
        System.out.println();       
    }
    System.out.println("-------------------------");
    for (AppTree node : breadthIterator(t))
    {
        System.out.printf("%20s    ", (node.isElement() ? "<"+((AppElement) node).getKind() : node.toString()));
        for (String s: pathToRoot(node)) System.out.print(s+"/");
        System.out.println();       
    }
  }
  
  public void testRecursive(AppTree t)
  { 
    if (t.isElement())
    {
       for (String s: pathToRoot(t)) System.out.print(s+"/");
           System.out.println();       
       for (AppTree subtree: t) testRecursive(subtree);
    }    
    
  }
  
  
}





