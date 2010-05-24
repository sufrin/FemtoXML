package femtoXML.app;

/**
 * $Id$
 * @author sufrin
 *
 */
public abstract class SimpleTemplate extends Template
{   public SimpleTemplate(Pred<Node> guard)        { super(guard); }
	public Stream<Node>  gen(Node target)          { return new Stream.Unit<Node>(genNode(target)); }
	public abstract Node genNode(Node target);
}
