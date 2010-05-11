package femtoXML.app;

public class Rule
{ Pred<Node> guard;
  Expr       result;
  public     Rule(Pred<Node> guard, Expr result) { this.guard=guard; this.result=result; }
  public     Value apply(Node target) 
  {
	  return guard.pass(target) ? (Node) result.eval(target) : null;
  }
}
