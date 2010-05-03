package femtoXML.app;

/**
 * Abstract precursor of all Node implementations
 * 
 * @author sufrin
 * 
 */

public abstract class NodeImp implements Node, Iterable<Node> {
	protected Element parent;

	public void setParent(Element parent) {
		this.parent = parent;
	}

	public Element getParent() {
		return parent;
	}

	protected Cursor<Node> Nil = new Cursor.Nil<Node>();

	public Cursor<Node> iterator() {
		return Nil;
	}

	/** Most trees are not elements: this yields false */
	public boolean isElement() {
		return false;
	}

	/** Most trees are not content: this yields false */
	public boolean isContent() {
		return false;
	}

	/**
	 * @return the value of a named attribute if this node has such an attribute
	 */
	public String getAttr(String attrName) {
		return null;
	}

	public String elementName() {
		return "-";
	}

	/** Returns a prefix order depth-first iterator */
	public Cursor<Node> prefixCursor() {
		return prefixCursor(null);
	}

	/**
	 * Returns a prefix order depth-first iterator cutting off below
	 * nodes that satisfy <code>cutoffBelow</code> if it is non-null
	 */
	public Cursor<Node> prefixCursor(final Pred<Node> cutoffBelow) {
		return new Cursor<Node>() {
			/** Acts as a stack */
			Cursor<Node> agenda = new Cursor.Unit<Node>(NodeImp.this);

			public boolean hasNext() {
				return agenda.hasNext();
			}

			public Node next() {
				assert (hasNext());
				Node result = agenda.next();
				// Push the subtrees onto the stack unless the cutoff is
				// here
				if (result.isElement()
						&& (cutoffBelow == null || !cutoffBelow.pass(result)))
					agenda = new Cursor.Cat<Node>(result.iterator(), agenda);
				return result;
			}
		};
	}

	/** Returns a breadth-first order iterator */
	public Cursor<Node> breadthCursor() {
		return breadthCursor(null);
	}

	/**
	 * Returns a breadth-first order iterator cutting off below
	 * nodes that satisfy <code>cutoffBelow</code> if it is non-null.
	 */
	public Cursor<Node> breadthCursor(final Pred<Node> cutoffBelow) {
		return new Cursor<Node>() {
			/** Acts as a queue */
			Cursor<Node> agenda = new Cursor.Unit<Node>(NodeImp.this);

			public boolean hasNext() {
				return agenda.hasNext();
			}

			public Node next() {
				assert (hasNext());
				Node result = agenda.next();
				// Queue the subtrees unless the cutoff is here
				if (result.isElement()
						&& (cutoffBelow == null || !cutoffBelow.pass(result)))
					agenda = new Cursor.Cat<Node>(agenda, result.iterator());
				return result;
			}
		};
	}

	/** Returns an iterator that yields the path back to the root (as Trees) */

	public Cursor<Node> pathToRoot() {
		final Node here = this;
		return new Cursor<Node>() {
			Node cursor = here;

			public boolean hasNext() {
				return cursor != null;
			}

			public Node next() {
				Node result = cursor;
				cursor = cursor.getParent();
				return result;
			}
		};
	}

}
