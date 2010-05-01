package femtoXML.app;

/** AppPred implements various forms of Pred<AppTree> */
public class AppPred
{
	/** Satisfied by nodes whose names match the given regular expression <code>pattern</code> */
	static public Pred<AppTree> isElementMatching(final String pattern)
	{
		return new Pred<AppTree>()
		{ public boolean pass(AppTree node) 
		  { return node.isElement() && node.elementName().matches(pattern); }
		};
	}
	
	/** Satisfied by nodes whose names match the given regular expression <code>pattern</code> */
	static public Pred<AppTree> isContentMatching(final String pattern)
	{
		return new Pred<AppTree>()
		{ public boolean pass(AppTree node) 
		  { return node.isContent() && node.toString().matches(pattern); }
		};
	}

	/** Satisfied by nodes with the given <code>name</code> */
	static public Pred<AppTree> isElement()
	{
		return new Pred<AppTree>()
		{ public boolean pass(AppTree node) 
		  { return node.isElement(); }
		};
	}

	/** Satisfied by nodes that are at least <code>levs</code> levels below a node satisfying <code>p</code> */
	static public Pred<AppTree> below(final Pred<AppTree> p, final int levs)
	{
		return new Pred<AppTree>()
		{ public boolean pass(AppTree node)
		  {
		    for (AppTree ancestor: node.pathToRoot().drop(levs))
			     if (p.pass(ancestor)) return true;
		    return false;
		  }
		};
	}
	
	/** Satisfied by nodes that contain a node satisfying <code>p</code> */
	static public Pred<AppTree> containing(final Pred<AppTree> p)
	{
		return new Pred<AppTree>()
		{ public boolean pass(AppTree node)
		  {
		    for (AppTree descendant: node.breadthIterator(p).drop(1))
			     if (p.pass(descendant)) return true;
		    return false;
		  }
		};
	}
	
}
