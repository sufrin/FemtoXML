package femtoXML.app;

/** AppPred = Pred<AppTree> */
public abstract class AppPred extends Pred<AppTree> 
{
	/** Satisfied by nodes whose names match the given regular expression <code>pattern</code> */
	static public AppPred isElementMatching(final String pattern)
	{
		return new AppPred()
		{ public boolean pass(AppTree node) 
		  { return node.isElement() && node.elementName().matches(pattern); }
		};
	}
	
	/** Satisfied by nodes whose names match the given regular expression <code>pattern</code> */
	static public AppPred isContentMatching(final String pattern)
	{
		return new AppPred()
		{ public boolean pass(AppTree node) 
		  { return node.isContent() && node.toString().matches(pattern); }
		};
	}

	/** Satisfied by nodes with the given <code>name</code> */
	static public AppPred isElement()
	{
		return new AppPred()
		{ public boolean pass(AppTree node) 
		  { return node.isElement(); }
		};
	}

	/** Satisfied by nodes that are at least <code>drop</code> levels below a node satisfying <code>p</code> */
	static public AppPred below(final AppPred p, final int drop)
	{
		return new AppPred()
		{ public boolean pass(AppTree node)
		  {
		    for (AppTree ancestor: node.pathToRoot().drop(drop))
			     if (p.pass(ancestor)) return true;
		    return false;
		  }
		};
	}

}
