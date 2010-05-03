package femtoXML.app;

import femtoXML.XMLAttrMap;
import femtoXML.XMLAttributes;
import femtoXML.XMLTreeFactory;
import femtoXML.XMLHandler.XMLLocator;

/**
 * A <code>XMLTreeFactory</code> that builds <code>Element, Comment, PI, Content</code> nodes as an XML file is parsed.
 * All optional features (pis, comments, doctypes) are recorded in the tree.
 * @author sufrin
 */
public class TreeFactory implements XMLTreeFactory<Node>
{ 
  /** True if entities have been expanded */
  protected boolean expandedEntities = true;
  
  /** Construct an TreeFactory
   * 
   * @param expandedEntitities -- true if entities will have been expanded before the tree nodes are built
   */
  public TreeFactory(boolean expandedEntities) { this.expandedEntities = expandedEntities; }
  
  /** <code>this(true)</code> */
  public TreeFactory() { this(true); }
  
  public Element newElement(String kind, XMLAttributes atts)
  {
    return new Element(kind, atts);
  }

  public Element newRoot()
  {
    return newElement("", new XMLAttrMap());
  }

  public Content newContent(String name, boolean cdata)
  {
    return new Content(name, cdata, expandedEntities);
  }

  public Node newComment(String data)
  {
    return new Comment(data);
  }

  public Node newDOCTYPE(String data)
  {
    return new DOCTYPE(data);
  }

  public Node newPI(String data)
  {
    return new PI(data);
  }

  public boolean wantComment()
  {
    return true;
  }
  
  public boolean wantPI()
  {
    return true;
  }
  
  public boolean wantDOCTYPE()
  {
    return true;
  }

  public Node newSpaces(String text)
  {
    return new Spaces(text);
  }

  public boolean wantSpaces()
  {
    return false;
  }
  
  XMLLocator locator;
  
  public void setLocator(XMLLocator locator)
  {
    this.locator = locator; 
  }
  
  public XMLLocator getLocator() 
  { return locator; }

}

