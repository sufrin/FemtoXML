package femtoXML.app;

/**
 * A form of iterator whose elements are generated by an independent
 * thread. This is useful in situation where the ''natural'' way of
 * programming the generation of the elements is recursive, but where
 * it is inconvenient or error-prone to program the recursion
 * housekeeping explicitly.
 * 
 * The following example generates all the elements in a tree that are
 * more than K levels below the root.
 * 
 * <pre>
 * 
 *    Cursor&lt;Node&gt; gen = new Generator&lt;Node&gt;();
 *       {   void generate(Node n, int l) { if (!closed &amp;&amp; l&gt;K) { put(n); generate(n.left, l+1); generate(n.right, l+1); } }
 *           
 *           public void gen() { generate(root, 0); }
 *       };
 * 
 * </pre>
 * 
 * The implementation is unbuffered: each <code>put</code> in the
 * generator thread is synchronized with the corresponding
 * <code>hasNext()/next()</code> from the consuming thread. The
 * consumer can signal the generator that it has no further interest
 * by calling <code>close()</code>.
 * */

abstract public class Generator<T> extends Stream<T>
{   SyncChan<T> chan = new SyncChan<T>(); // channel from generator to iterator
	Thread thread = null;
	Runnable body = null;
	/** Starts false; becomes true when the stream is closed. */
	protected boolean closed = false;

	/**
	 * Construct an instance of the generator and start generating
	 * immediately
	 */
	public Generator()
	{
		body = new Runnable()
		{
			public void run()
			{
				try
				{
			      gen();
				} catch (SyncChan.Closed e)
				{
				}
				chan.close();
				closed = true;
			}
		};
		thread = new Thread(body);
		//System.err.println("Starting " + this);
		thread.start();
	}

	public void close()
	{
		chan.close();
		closed = true; 
	}

	/** Generate an element */
	protected void put(T t)
	{
		try { chan.write(t); } catch (SyncChan.Closed ex) { closed = true; }
	}

	/**
	 * This must be defined. It should generate and deliver (all the)
	 * elements using put
	 */
	abstract public void gen();
    
	/** Unsupported. Use <code>vector()</code> on the original */
	public Stream<T> copy()
	{
		throw new UnsupportedOperationException();
	}

	public boolean hasNext()
	{
		return chan.hasNext();
	}

	/**
	 * Returns the next generated element
	 */
	public T next()
	{
		return chan.next();
	}

	public static void main(String[] arg)
	{
		Stream<Integer> g = new Generator<Integer>()
		{
			public void gen()
			{
				System.err.println(this);
				for (int i = 0; i < 20; i++)
					put(i);
			}
		};
		int i = 0;
		while (g.hasNext() && i++ < 15)
		{
			System.out.println(g.next());
			System.out.flush();
		}
		;
		g.close();
		Stream<Integer> g1 = g.copy();
		while (g1.hasNext())
		{
			System.out.println(g1.next());
			System.out.flush();
		}
	}

}
