package femtoXML.app;

/*
 *  A rule is a guarded expression that maps a target Node to a Cursor<Node>. 
 *  When evaluated at a target, a rule first evaluates its guard's pass method at
 *  the target. 
 *  
 *  If the pass method yields true then the generate method is evaluated; otherwise the
 *  rule yields the empty Cursor.
 *  
 *  $Rev: $
 */
public abstract class Template extends Pred<Node> implements Expr<Node,Stream<Node>>
{ Pred<Node> guard;

  // public Pred<Node> getGuard() { return guard; }
  
  /** Evaluated at targets that satisfy the guard. */
  abstract   public Stream<Node> gen(Node target);
  
  public     Template(Pred<Node> guard) { this.guard=guard; }
  
  private static Stream<Node> Nil = new Stream.Nil<Node>();
  
  /** Evaluate the expression if the target satisfies the guard; else return Nil. */
  public     Stream<Node> eval(Node target) 
  {
	  return guard.pass(target) ? gen(target) : Nil;
  }
  
  /** Does the target satisfy the guard? */
  public boolean pass(Node target) { return guard.pass(target); }
  
  /** Return <code>orElse(this, other)</code>. */
  public Template orElse(Template other) { return orElse(this, other); }
  
  /** A rule that attempts <code>a</code>; if this doesn't succeed
   *  then the rule attempts <code>b</code>. 
   */
  public static Template orElse(final Template a, final Template b)
  {
    return new Template(a.guard.or(b.guard))
    {
    	   public Stream<Node> gen(Node target)
    	   {   Stream<Node> result;
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
 
  public static Template ALWAYS = new Template(NodePred.TRUE) { public Stream<Node> gen(Node t) { return new Stream.Unit<Node>(t); } };

}
