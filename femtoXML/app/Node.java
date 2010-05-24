package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Supertype of all tree nodes.
 * 
 * @author sufrin
 * 
 */
public interface Node extends Iterable<Node>
{
	/**
	 * Default visitor that constructs an isomorphic copy
	 */
	public static class CopyVisitor implements Visitor
	{
		public Node visit(Comment c)
		{
			return c.copy();
		}

		public Node visit(Content p)
		{
			return p.copy();
		}

		public Node visit(DOCTYPE d)
		{
			return d.copy();
		}

		public Node visit(Element e)
		{
			return e.copy();
		}

		public Node visit(PI p)
		{
			return p.copy();
		}

		public Node visit(Spaces s)
		{
			return s.copy();
		}
	}

	/**
	 * Methods to apply during a visit
	 */
	public static interface Visitor
	{
		Node visit(Comment c);

		Node visit(Content p);

		Node visit(DOCTYPE d);

		Node visit(Element e);

		Node visit(PI p);

		Node visit(Spaces s);
	}

	/**
	 * @return the immediate subtrees of this node if there are any
	 */
	Stream<Node> body();

	/**
	 * @return all the subtrees of this node if there are any (breadth
	 *         first order)
	 */
	Stream<Node> breadthCursor();

	/**
	 * @return all the subtrees of this node if there are any (breadth
	 *         first order) cutting off below nodes that 
	 *         pass <code>cutoffBelow</code>.
	 */
	Stream<Node> breadthCursor(Pred<Node> cutoffBelow);

	/**
	 * Make a fresh, isomorphic, copy of this node
	 */
	Node copy();

	/**
	 * @return the element name if it is an element
	 * 
	 */
	String elementName();

	/**
	 * @return the value of a named attribute if this node has such an
	 *         attribute
	 */
	String getAttr(String attrName);

	/**
	 * @return the parent of this node
	 */
	Element getParent();

	/**
	 * @return true if this node is a lump of text with no internal
	 *         structure.
	 */
	boolean isContent();

	/**
	 * @return true if it's an element
	 */
	boolean isElement();

	/**
	 * @return true if this node is content but not CDATA content.
	 */
	boolean isWord();

	/**
	 * @return the immediate subtrees of this node if there are any
	 */
	Stream<Node> iterator();

	/**
	 * @return the path back to the root
	 */
	Stream<Node> pathToRoot();

	/**
	 * @return all the subtrees of this node if there are any (depth
	 *         first prefix order)
	 */
	Stream<Node> prefixCursor();

	/**
	 * @return all the subtrees of this node if there are any (depth
	 *         first prefix order)  cutting off below nodes that 
	 *         pass <code>cutoffBelow</code>.
	 */
	Stream<Node> prefixCursor(Pred<Node> cutoffBelow);

	/**
	 * Write this tree node on the given writer at the given
	 * indentation in a form suitable for re-reading. If
	 * <code>indent&lt;=0</code> then don't use any indentation at
	 * all.
	 */
	void printTo(FormatWriter out, int indent);

	/**
	 * Set the parent of this node
	 */
	void setParent(Element parent);

	/**
	 * Visit this node with the appropriate method of the
	 * <code>Visitor v</code>
	 * 
	 * @return
	 */
	Node visit(Visitor v);
}
