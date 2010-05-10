package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Supertype of all tree nodes.
 * 
 * @author sufrin
 * 
 */
public interface Node extends Iterable<Node>, Value
{
	/**
	 * Write this tree node on the given writer at the given
	 * indentation in a form suitable for re-reading. If
	 * <code>indent&lt;=0</code> then don't use any indentation at
	 * all.
	 */
	void printTo(FormatWriter out, int indent);

	/**
	 * @return true if this node is content but not CDATA content.
	 */
	boolean isWord();

	/**
	 * @return the value of a named attribute if this node has such an
	 *         attribute
	 */
	String getAttr(String attrName);

	/**
	 * @return true if this node is a lump of text with no internal
	 *         structure.
	 */
	boolean isContent();

	/**
	 * Set the parent of this node
	 */
	void setParent(Element parent);

	/**
	 * @return the parent of this node
	 */
	Element getParent();

	/**
	 * @return all the subtrees of this node if there are any (depth
	 *         first prefix order)
	 */
	Cursor<Node> prefixCursor();

	/**
	 * @return all the subtrees of this node if there are any (breadth
	 *         first order)
	 */
	Cursor<Node> breadthCursor();

	/**
	 * @return all the subtrees of this node if there are any (depth
	 *         first prefix order)
	 */
	Cursor<Node> prefixCursor(Pred<Node> cutoffBelow);

	/**
	 * @return all the subtrees of this node if there are any (breadth
	 *         first order)
	 */
	Cursor<Node> breadthCursor(Pred<Node> cutoffBelow);

	/**
	 * @return the immediate subtrees of this node if there are any
	 */
	Cursor<Node> iterator();

	/**
	 * @return the path back to the root
	 */
	Cursor<Node> pathToRoot();

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
