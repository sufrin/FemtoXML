package femtoXML.app;
import java.util.concurrent.SynchronousQueue;

/** A form of iterator whose elements are generated by an independent thread. This is useful
 *  in situation where the ''natural'' way of programming the generation of the elements is
 *  recursive, but where it is inconvenient or error-prone to program the recursion 
 *  housekeeping explicitly.   
 *  
 *  The following example generates all the elements in a tree that are more than K levels below the root.
 * <pre>
 * <code>
 *    Cursor<Node> gen = new Generator<Node>();
      {   void generate(Node n, int l) { if (!closed && l>K) { put(n); generate(n.left, l+1); generate(n.right, l+1); } }
          
          public void gen() { generate(root, 0); }
      };
 * </code>
 * </pre>
 * 
 * The implementation is unbuffered: each <code>put</code> in the generator thread is synchronized with the corresponding <code>next()</code>
 * from the consuming thread. The consumer can signal the generator that it has no further interest by calling <code>close()</code>.
 * */

public abstract class Generator<T> extends Cursor<T> implements Runnable {
	
    SynchronousQueue<T> chan = new SynchronousQueue<T>(); // channel from generator to iterator
    boolean closed = false;
    Thread  thread = null;
    
    /** Construct an instance of the generate and start generating immediately */
    public Generator() {
      thread = new Thread(this);
      thread.start();
    }
    
    public void close() { closed = true; }
    
    /** Generate an element */
    protected void put(T t) { try {
		if (closed) throw new IllegalStateException(); else chan.put(t);
	} catch (InterruptedException e) {
		e.printStackTrace();
		throw new IllegalStateException();
	} }
	
    /** This must be defined. It should generate all elements */
    abstract public void gen();
    
    public void run() { try { gen(); } catch (IllegalStateException e) { close(); } }
    
    public boolean hasNext() { return !closed; }
    
    public T next() {
    	if (closed) throw new IllegalStateException(); 
			try {
				return chan.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
    }
    
    
    public static void main(String[] arg)
    {
      Cursor<Integer> gen = new Generator<Integer>()
      {
    	  public void gen()
    	  {
    		  for (int i=0; !closed && i<20; i++) put(i);
    	  }
      };
      while (gen.hasNext()) System.err.println(gen.next());
    }

}
