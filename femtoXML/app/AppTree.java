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
   * @return true if this node is a lump of text with no internal structure.
   */
  boolean isWord();
  
  /**
   * Set the parent of this node
   */
  void setParent(AppElement parent);
  
  /**
   * @return the parent of this node
   */
  AppElement getParent();
  
  /**
   * @return the subtrees of this node if there are any 
   */
  AppIterator<AppTree> iterator();
  
  /**
   * @return true if it's an element
   */
  boolean isElement();
}

