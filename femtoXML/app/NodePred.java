package femtoXML.app;

/** NodePred implements various forms of Pred<Node> */
public class NodePred
{
	/** Satisfied by nodes whose names match the given regular expression <code>pattern</code> */
	static public Pred<Node> isElementMatching(final String pattern)
	{
		return new Pred<Node>()
		{ public boolean pass(Node node) 
		  { return node.isElement() && node.elementName().matches(pattern); }
		};
	}
	
	/** Satisfied by elements with named attributes that match the given regular expression <code>pattern</code> */
	static public Pred<Node> hasAttr(final String attrName, final String pattern)
	{
		return new Pred<Node>()
		{ public boolean pass(Node node) 
		  { if (node.isElement())
		    { String aval = node.getAttr(attrName);
		      return aval!=null && aval.matches(pattern);
		    }
		    else
		      return false;
	      }
		};
	}
	
	/** Satisfied by content nodes whose names match the given regular expression <code>pattern</code> */
	static public Pred<Node> isContentMatching(final String pattern)
	{
		return new Pred<Node>()
		{ public boolean pass(Node node) 
		  { return node.isContent() && node.toString().matches(pattern); }
		};
	}

	/** Satisfied by nodes with the given <code>name</code> */
	static public Pred<Node> isElement()
	{
		return new Pred<Node>()
		{ public boolean pass(Node node) 
		  { return node.isElement(); }
		};
	}

	/** Satisfied by nodes that are at least <code>levs</code> levels below a node satisfying <code>p</code> */
	static public Pred<Node> below(final Pred<Node> p, final int levs)
	{
		return new Pred<Node>()
		{ public boolean pass(Node node)
		  {
		    for (Node ancestor: node.pathToRoot().drop(levs))
			     if (p.pass(ancestor)) return true;
		    return false;
		  }
		};
	}
	
	/** Satisfied by nodes that contain a node satisfying <code>p</code> */
	static public Pred<Node> containing(final Pred<Node> p)
	{
		return new Pred<Node>()
		{ public boolean pass(Node node)
		  {
		    for (Node descendant: node.breadthCursor(p).drop(1))
			     if (p.pass(descendant)) return true;
		    return false;
		  }
		};
	}
	
}
