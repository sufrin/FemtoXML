package femtoXML.app;

public class Rule
{ Pred<Node> guard;
  Expr<Node>       result;
  public     Rule(Pred<Node> guard, Expr<Node> result) { this.guard=guard; this.result=result; }
  public     Value apply(Node target) 
  {
	  return guard.pass(target) ? (Node) result.eval(target) : null;
  }
}
