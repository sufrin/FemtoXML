package femtoXML.app;

public abstract class SimpleRule extends Rule
{   public SimpleRule(Pred<Node> guard)        { super(guard); }
	public Cursor<Node>  generate(Node target) { return new Cursor.Unit<Node>(generateOne(target)); }
	public abstract Node generateOne(Node target);
}
