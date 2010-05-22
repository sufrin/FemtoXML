package femtoXML.app;

/*
 *  A rule is a guarded Node-valued expression. When applied
 *  at a target Node, it yields null if <code>guard.pass(target)</code> is false; otherwise 
 *  it yields the result of <code>eval(target)</code>. A rule is said
 *  to succeed at a target if its <code>apply</code> method yields a
 *  non-null value.
 *
 */
public abstract class Rule implements Expr<Node,Node>
{ Pred<Node> guard;

  public Pred<Node> getGuard() { return guard; }
  
  /** The expression that is evaluated at targets that satisfy the guard. */
  abstract   public Node eval(Node target);
  
  public     Rule(Pred<Node> guard) { this.guard=guard; }
  
  /** Evaluate the expression of the target satisfies the guard; else return null. */
  public     Node apply(Node target) 
  {
	  return guard.pass(target) ? eval(target) : null;
  }
  
  /** Does the target satisfy the guard? */
  public 	boolean pass(Node target) { return guard.pass(target); }
  
  /** Return <code>orElse(this, other)</code>. */
  public Rule orElse(Rule other) { return orElse(this, other); }
  
  /** A rule that attempts <code>a</code>; if this doesn't succeed
   *  then the rule attempts <code>b</code>. 
   */
  public static Rule orElse(final Rule a, final Rule b)
  {
    return new Rule(a.guard.or(b.guard))
    {
    	   public Node eval(Node target)
    	   {
    		   Node result = null;
    		   if (a.pass(target)) result = a.eval(target);
    		   if (result==null && b.pass(target)) result = b.eval(target);
    		   return result;
    	   }
    };
  }

  public static Rule andThen(final Rule a, final Rule b)
  {
    return new Rule(a.guard.or(b.guard))
    {
    	   public Node eval(Node target)
    	   {
    		   Node result = null;
    		   if (a.pass(target)) result = a.eval(target);
    		   if (result==null && b.pass(target)) result = b.eval(target);
    		   return result;
    	   }
    };
  } 
  
  public static Rule ALWAYS = new Rule(NodePred.TRUE) { public Node eval(Node t) { return t; } };

}
