package femtoXML.app;

/**
 * $Id$
 *
 */
public abstract class NodeTemplate extends Template
{   public NodeTemplate(Pred<Node> guard)        { super(guard); }
	public Stream<Node>  gen(Node target)          { return new Stream.Unit<Node>(genNode(target)); }
	public abstract Node genNode(Node target);
}
