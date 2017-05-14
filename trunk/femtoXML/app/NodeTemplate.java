package femtoXML.app;

/**
 * $Id$
 *
 */
public abstract class NodeTemplate extends Template
{   public NodeTemplate(Pred<Node> guard)        { super(guard); }
	
    /** Wraps a non-null result of <code>genNode</code> in a unit stream and returns that stream.
     *  A <code>null</code> gets returned as an empty stream.
     */
    public Stream<Node>  gen(Node target)        
	{ Node result = genNode(target);
	  return result==null ? Nil : new Stream.Unit<Node>(result); 
	}
	/** Generate the <code>Node</code> that will comprise the single element of the stream
	 *  that will be returned by this template's <code>gen</code> method. 
	 *  For convenience, <code>null</code> translates to an empty stream.
	 */
	public abstract Node genNode(Node target);
}
