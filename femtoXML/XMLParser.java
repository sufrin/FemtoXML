package femtoXML;

import java.util.*;
/**
 * An <code>XMLParser</code> is an implementation of an <code>XMLHandler</code> that generates parse-trees.
 * 
 * @author sufrin
 *
 * @param <T> -- The type of the parse-tree that will be constructed.
 */

public class XMLParser<T> implements XMLHandler
{
  protected XMLTreeFactory<T> factory;

  public XMLParser(XMLTreeFactory<T> factory)
  {
    this.factory = factory;
  }
  
  /** The stack of parse-tree nodes corresponding to unclosed elements. */
  protected Stack<XMLComposite<T>> stack = new Stack<XMLComposite<T>>();
  
  /** The stack of names of unclosed elements. Invariant: <code>stack.size()==kinds.size()</code> */
  protected Stack<String>          kinds = new Stack<String>();

  public void startElement(String kind, Map<String, String> atts)
  {
    stack.push(factory.newElement(kind, atts));
    kinds.push(kind);
  }

  /**
   * Process an end element tag that matches the enclosing start element tag;
   * throw an XMLSyntaxError error otherwise. Subclasses can override this with a
   * more intelligent or forgiving method that simulates the insertion of end
   * tags, or obeys the more relaxed rules of HTML.
   */
  public void endElement(String kind)
  {
    String tkind = kinds.pop();
    if (tkind.equals(kind))
    {
      T top = stack.pop().close();
      stack.peek().addTree(top);
    }
    else
      throw new XMLSyntaxError(String.format("Non-nested: <%s>...</%s>", tkind, kind));
  }

  protected T theTree = null;

  public T getTree()
  {
    return theTree;
  }

  public void commentCharacters(CharSequence text)
  {
    if (factory.canComment())
       stack.peek().addTree(factory.newComment(text.toString()));
  }

  public void wordCharacters(CharSequence text)
  {
    stack.peek().addTree(factory.newWord(text.toString()));
  }

  public void startDocument()
  {
    stack.clear();
    stack.push(factory.newRoot());
    kinds.clear();
    kinds.push("");
  }

  public void endDocument()
  {
    switch (stack.size())
    {
      case 1:
        theTree = stack.peek().close();
        break;
      case 0:
        throw new XMLSyntaxError("Document has no elements.");
      default:
        throw new XMLSyntaxError(String.format("Premature end of document in unclosed <%s>",  kinds.peek()));
    }
  }

  /** This procedure returns null and must be overridden in a subclass if entities other
   * than the standard few built-in entities are to be expanded. */
   
  public String decodeEntity(String entity)
  {
    return null;
  }
}
