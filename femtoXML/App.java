package femtoXML;
import java.io.*;
public class App
{
  public static void main (String[] args) throws Exception
  { XMLParser<AppTree> parser  = new XMLParser<AppTree>(new AppTreeFactory());
    XMLScanner         scanner = new XMLScanner(parser);
    for (String arg: args)
    { scanner.read(new FileReader(arg));
      AppElement root = (AppElement) parser.getTree();
      for (AppTree tree: root) tree.printTo(System.out, 0);
      System.out.println();
      System.out.flush();
    }
  }
}

