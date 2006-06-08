package femtoXML;

import java.io.Reader;
import java.util.*;

/**
 * An <code>XMLParser&lt;T&gt;</code> is an <code>XMLHandler</code> that can be 
 * repeatedly used to generate application structures
 * from XML documents.
 * 
 * @author sufrin ($Revision$)
 *
 * @param <T> -- The type of application tree that will be constructed.
 */

public class XMLParser<T> implements XMLHandler
{
  protected XMLTreeFactory<T> factory;

  /**
   * Construct a parser.
   * @param factory -- the factory that will map the individual XML elements 
   * into application substructures.
   */
  public XMLParser(XMLTreeFactory<T> factory)
  {
    this.factory = factory;
  }
  
  /** The stack of parse-tree nodes corresponding to unclosed elements. */
  protected Stack<XMLComposite<T>> stack = new Stack<XMLComposite<T>>();
  
  /** The stack of names of unclosed elements. 
      Invariant: <code>stack.size()==kinds.size()==lines.size()</code> 
  */
  protected Stack<String>          kinds = new Stack<String>();
  
  /** The stack of starting line-numbers of the unclosed elements */
  protected Stack<Integer>         lines = new Stack<Integer>();
  
  /**
   *  The stack of XMLAttributes corresponding to unclosed elements
   */
  protected Stack<XMLAttributes> attrs = new Stack<XMLAttributes>();

  public void startElement(String kind, XMLAttributes atts)
  {
    stack.push(factory.newElement(kind, atts));
    kinds.push(kind);
    lines.push(locator.lineNumber());
    attrs.push(atts);
  }

  /**
   * Process an end element tag that matches the enclosing start element tag;
   * throw an <code>XMLSyntaxError</code> error otherwise. Subclasses can
   * override this with a more intelligent or forgiving method that simulates
   * the insertion of end tags, or obeys the more relaxed rules of HTML.
   */
  public void endElement(String kind)
  {
    String tkind = kinds.pop();
    if (tkind.equals(kind))
    {
      T top = stack.pop().close();
      current().add(top);
      lines.pop();
      attrs.pop();
    }
    else
      throw new XMLSyntaxError(locator,
                               String.format("Improperly nested element: <%s>@%s...</%s>@%s",
                                             tkind,
                                             lines.peek(),
                                             kind,
                                             locator.lineNumber()));
  }
  
  protected final XMLComposite<T> current() { return stack.peek(); }

  protected T theTree = null;
  
  /** Get the parse-tree */
  public T getTree()
  {
    return theTree;
  }

  public void commentCharacters(CharSequence text)
  {
    if (factory.wantComment())
       current().add(factory.newComment(text.toString()));
  }

  public void contentCharacters(CharSequence text, boolean cdata)
  {
    current().add(factory.newContent(text.toString(), cdata));
  }

  public void PICharacters(CharSequence text)
  {
    if (factory.wantPI())
       current().add(factory.newPI(text.toString()));
  }
  
  public void DOCTYPECharacters(CharSequence text)
  {
    if (factory.wantDOCTYPE())
       current().add(factory.newDOCTYPE(text.toString()));
  }

  public void startDocument()
  {
    stack.clear();
    stack.push(factory.newRoot());
    kinds.clear();
    kinds.push("");
    lines.clear();
    lines.push(1);
    attrs.clear();
    attrs.push(rootAttributes());
    theTree = null;
  }
  
  public XMLAttributes rootAttributes()
  {
    return new XMLAttributes()
    { 
      public String get(String key) { return null; }
      
      public String put(String key, String value) { return ""; }
      
      public void printTo(FormatWriter out, int indent) { out.println(toString()); }
      
      public String toString() { return ""; }
    };
  }

  public void endDocument()
  {
    switch (stack.size())
    {
      case 1:
        theTree = current().close();
        break;
      case 0:
        throw new XMLSyntaxError(locator, String.format("Document has no elements."));
      default:
        throw new XMLSyntaxError(locator, 
                                 String.format("Premature end of document in unclosed <%s>@%d", 
                                               kinds.peek(), 
                                               lines.peek()));
    }
  }

  /**
   * Default returns null and must be overridden in a subclass if
   * non-character entities are to be expanded. When overridden it should return
   * a reader that yields the expansion of the named entity.
   */
   
  public Reader decodeEntity(String entityName)
  {
    return null;
  }
  
  /**
   * Default uses <code>XMLCharUtil.decodeCharEntity</code> to decode character entity
   * names.
   */
  public char decodeCharEntity(String entityName)
  { 
    return XMLCharUtil.decodeCharEntity(entityName);
  }


  XMLLocator locator;
  
  public void setLocator(XMLLocator locator)
  {
    this.locator = locator; 
    factory.setLocator(locator);
  }
  
  public XMLLocator getLocator() 
  { return locator; }
  
  /**
   * Default returns an
   * <code>XMLAttrMap</code> with <code>expandEntities</code> set appropriiately.
   */
  public XMLAttributes newAttributes(boolean expandEntitites)
  {
    return new XMLAttrMap().setExpandedEntities(expandEntitites);
  }

  public void spaceCharacters(CharSequence text)
  {
    current().add(factory.newSpaces(text.toString()));    
  }
  
  /**
   * Default delegates to <code>factory</code>. To be discriminating about
   * space-recording, either override this with a method that inspects the topmost
   * element on the stack to see if it wants spaces to be recorded or supply a
   * factory that does likewise.
   */
  public boolean wantSpaces()
  {
    return factory.wantSpaces();
  }
  
  /**
   * Default delegates to <code>factory</code>.
   */
  public boolean wantComment()
  {
    return factory.wantComment();
  }
  
  /**
   * Default delegates to <code>factory</code>.
   */
  public boolean wantPI()
  {
    return factory.wantPI();
  }
  
  /**
   * Default delegates to <code>factory</code>.
   */
  public boolean wantDOCTYPE()
  {
    return factory.wantDOCTYPE();
  }
}

