package femtoXML.app;

/*
 *  A rule is a guarded expression that maps a target Node to a Cursor<Node>. 
 *  When evaluated at a target, a rule first evaluates its guard's pass method at
 *  the target. 
 *  
 *  If the pass method yields true then the generate method is evaluated; otherwise the
 *  rule yields the empty Cursor.
 */
public abstract class Rule extends Pred<Node> implements Expr<Node,Cursor<Node>>
{ Pred<Node> guard;

  // public Pred<Node> getGuard() { return guard; }
  
  /** Evaluated at targets that satisfy the guard. */
  abstract   public Cursor<Node> generate(Node target);
  
  public     Rule(Pred<Node> guard) { this.guard=guard; }
  
  private static Cursor<Node> Nil = new Cursor.Nil<Node>();
  
  /** Evaluate the expression if the target satisfies the guard; else return Nil. */
  public     Cursor<Node> eval(Node target) 
  {
	  return guard.pass(target) ? generate(target) : Nil;
  }
  
  /** Does the target satisfy the guard? */
  public boolean pass(Node target) { return guard.pass(target); }
  
  /** Return <code>orElse(this, other)</code>. */
  public Rule orElse(Rule other) { return orElse(this, other); }
  
  /** A rule that attempts <code>a</code>; if this doesn't succeed
   *  then the rule attempts <code>b</code>. 
   */
  public static Rule orElse(final Rule a, final Rule b)
  {
    return new Rule(a.guard.or(b.guard))
    {
    	   public Cursor<Node> generate(Node target)
    	   {   Cursor<Node> result;
    		   if (a.pass(target))
    			  result = a.eval(target); 
    		   else 
    		   if (b.pass(target)) 
    			  result = b.eval(target);
    		   else 
    			  result = Nil;
    		   return result;
    	   }
    };
    
   }
 
  public static Rule ALWAYS = new Rule(NodePred.TRUE) { public Cursor<Node> generate(Node t) { return new Cursor.Unit<Node>(t); } };

}
