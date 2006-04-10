package femtoXML.app;

import femtoXML.XMLAttributes;
import femtoXML.XMLTreeFactory;

/**
 * A <code>XMLTreeFactory</code> that builds <code>AppElement, AppComment, AppPI, AppWord</code> nodes as an XML file is parsed.
 * @author sufrin
 */
public class AppTreeFactory implements XMLTreeFactory<AppTree>
{ 
  /** True if entities have been expanded */
  protected boolean expandedEntities = true;
  
  /** Construct an AppTreeFactory
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
    return newElement("", null);
  }

  public AppWord newWord(String name, boolean cdata)
  {
    return new AppWord(name, cdata, expandedEntities);
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

  public boolean canComment()
  {
    return true;
  }
  
  public boolean canPI()
  {
    return true;
  }
  
  public boolean canDOCTYPE()
  {
    return true;
  }

  public AppTree newSpaces(String text)
  {
    return new AppSpaces(text);
  }

  public boolean wantSpaces(String elementKind)
  {
    return false;
  }
}

