package femtoXML.app;

import femtoXML.XMLAttrMap;
import femtoXML.XMLAttributes;
import femtoXML.XMLTreeFactory;
import femtoXML.XMLHandler.XMLLocator;

/**
 * A <code>XMLTreeFactory</code> that builds <code>AppElement, AppComment, AppPI, AppContent</code> nodes as an XML file is parsed.
 * All optional features (pis, comments, doctypes) are recorded in the tree.
 * @author sufrin
 */
public class AppTreeFactory implements XMLTreeFactory<AppTree>
{ 
  /** True if entities have been expanded */
  protected boolean expandedEntities = true;
  
  /** Construct an AppTreeFactory
   * 
   * @param expandedEntitities -- true if entities will have been expanded before the tree nodes are built
   */
  public AppTreeFactory(boolean expandedEntities) { this.expandedEntities = expandedEntities; }
  
  /** <code>this(true)</code> */
  public AppTreeFactory() { this(true); }
  
  public AppElement newElement(String kind, XMLAttributes atts)
  {
    return new AppElement(kind, atts);
  }

  public AppElement newRoot()
  {
    return newElement("", new XMLAttrMap());
  }

  public AppContent newContent(String name, boolean cdata)
  {
    return new AppContent(name, cdata, expandedEntities);
  }

  public AppTree newComment(String data)
  {
    return new AppComment(data);
  }

  public AppTree newDOCTYPE(String data)
  {
    return new AppDOCTYPE(data);
  }

  public AppTree newPI(String data)
  {
    return new AppPI(data);
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

  public AppTree newSpaces(String text)
  {
    return new AppSpaces(text);
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

