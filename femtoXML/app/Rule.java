package femtoXML.app;

/*
 *  A rule is a guarded template.
 *
 */
public abstract class Rule
{ Pred<Node> guard;
  abstract public Node rewrite(Node target);
  public     Rule(Pred<Node> guard) { this.guard=guard; }
  public     Node apply(Node target) 
  {
	  return guard.pass(target) ? rewrite(target) : null;
  }
}
