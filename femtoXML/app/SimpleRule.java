package femtoXML.app;

public abstract class SimpleRule extends Rule
{   public SimpleRule(Pred<Node> guard)        { super(guard); }
	public Stream<Node>  generate(Node target) { return new Stream.Unit<Node>(generateOne(target)); }
	public abstract Node generateOne(Node target);
}
