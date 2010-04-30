package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Supertype of all tree nodes.
 * @author sufrin
 *
 */
public interface AppTree extends Iterable<AppTree>
{
  /**
   * Write this tree node on the given writer at the given indentation in a form
   * suitable for re-reading. If <code>indent&lt;=0</code> then don't use any
   * indentation at all.
   */
  void printTo(FormatWriter out, int indent);
  
  /**
   * @return true if this node is content but not CDATA content.
   */
  boolean isWord();
  
  /**
   * @return true if this node is a lump of text with no internal structure.
   */
  boolean isContent();
  
  /**
   * Set the parent of this node
   */
  void setParent(AppElement parent);
  
  /**
   * @return the parent of this node
   */
  AppElement getParent();
  
  /**
   * @return all the subtrees of this node if there are any (depth first prefix order) 
   */
  AppIterator<AppTree> prefixIterator();
  
  /**
   * @return all the subtrees of this node if there are any (breadth first order)
   */
  AppIterator<AppTree> breadthIterator();
  
  /**
   * @return the immediate subtrees of this node if there are any 
   */
  AppIterator<AppTree> iterator();
  
  /**
   * @return the path back to the root 
   */
  AppIterator<AppTree> pathToRoot();

  
  /**
   * @return true if it's an element
   */
  boolean isElement();
  
  /**
   * @return the element name if it is an element
   * 
   */
   String elementName();
}

