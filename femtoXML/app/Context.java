package femtoXML.app;
import java.util.*;

public class Context
{ Map<String, Value> map = new HashMap<String, Value>();
  public void  put(String name, Value val) { map.put(name, val); }
  public Value get(String name) { return map.get(name); }
  public Pred<Node> var(final String name, final Pred<Node> p) 
  {
	  return new Pred<Node>()
	  {
		  public boolean pass(Node node)
		  {
			  if (p.pass(node)) 
			     { put(name, node); return true; }
			  else 
				 return false;
		  }
	  };
  }
   
}
