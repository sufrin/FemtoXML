package femtoXML;
import java.util.*;

public class XMLParser<T> implements XMLHandler
{ protected XMLTreeFactory<T> factory; 
  public  XMLParser(XMLTreeFactory<T> factory) { this.factory=factory; }

  protected Stack<Composite<T>>  stack = new Stack<Composite<T>>();
  protected Stack<String>        kinds = new Stack<String>();
  // Invariant: stack.size()==kinds.size()
  
  public void startElement(String kind, Map<String,String> atts) 
  { stack.push(factory.newElement(kind, atts)); kinds.push(kind); }

  public void endElement(String kind) 
  { String tkind = kinds.pop();    
    if (tkind.equals(kind))
       { T top=stack.pop().close(); stack.peek().addTree(top); }
    else 
       throw new RuntimeException(String.format("Non-nested: <%s>...</%s>", tkind, kind));
  }
  
  // ...
  
  protected T theTree = null;
  public    T getTree()  { return theTree; }

  public void commentCharacters(CharSequence text)
  { if (factory.canComment()) stack.peek().addTree(factory.newComment(text.toString())); }

  public void   wordCharacters(CharSequence text) 
  { stack.peek().addTree(factory.newWord(text.toString())); }

  public void   startDocument()             
  { stack.clear(); stack.push(factory.newRoot()); kinds.clear(); kinds.push(""); }

  public void   endDocument()               
  { switch (stack.size())
    { case 1:  theTree = stack.peek().close(); break;
      case 0:  throw new RuntimeException("Document has no elements."); 
      default:
       throw new RuntimeException
          (String.format("Premature end of document in unclosed <%s>", kinds.peek()));
    }
  }

  public String decodeEntity(String entity) { return null; }
}

