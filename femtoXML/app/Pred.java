package femtoXML.app;

/**
 * Predicates and their algebra
 */
public abstract class Pred<T> {
	
	abstract public boolean pass(T t);

	public Pred<T> and(final Pred<T> other) {
		return new Pred<T>() {
			public boolean pass(T t) {
				return Pred.this.pass(t) && other.pass(t);
			}
		};
	}

	public Pred<T> or(final Pred<T> other) {
		return new Pred<T>() {
			public boolean pass(T t) {
				return Pred.this.pass(t) || other.pass(t);
			}
		};
	}

	public Pred<T> not() {
		return new Pred<T>() {
			public boolean pass(T t) {
				return !Pred.this.pass(t);
			}
		};
	}

	public static <T> Pred<T> TRUE() {
		return new Pred<T>() {
			public boolean pass(T t) {
				return true;
			}
		};
	}

	public static <T> Pred<T> FALSE() {
		return new Pred<T>() {
			public boolean pass(T t) {
				return false;
			}
		};
	}

	/** @see Pred.cache() */
	public static class Cached<T> extends Pred<T> {
		/** Used for instrumentation */
		public long cachemisses = 0, hits = 0;
		T n1, n2, n3, n4;
		boolean b1, b2, b3, b4;
		Pred<T> cached;

		private Cached(Pred<T> cached) {
			this.cached = cached;
		}

		public boolean pass(T node) {
			hits++;
			if (node == n1)
				return b1;
			else if (node == n2)
				return b2;
			else if (node == n3)
				return b3;
			else if (node == n4)
				return b4;
			else {
				n4 = n3;
				b4 = b3;
				n3 = n2;
				b3 = b2;
				n2 = n1;
				b2 = b1;
				n1 = node;
				b1 = cached.pass(node);
				cachemisses++;
			}
			return b1;
		}
	}

	/**
	 * Transforms a predicate into one that caches very recent results. A cached
	 * predicate may only be used in contexts where the objects it is applied to
	 * are immutable (modulo the predicate), but we don't check for this
	 * dynamically -- though it would be possible to do so at the cost of
	 * throwing away the efficiency offered by caching.
	 * 
	 * Typical usage is with a tree traversal filtered by a predicate P that is
	 * also used to cut-off the traversal. For example, when filtering for the
	 * topmost nodes in a tree that are below a node that satisfies P the
	 * pattern of use is:
	 * <code>for Node n :tree.depthFirst(cutoffBelow=p).filter(p))</code>
	 * 
	 * On a miss the least-recently-used cache entry is purged.
	 */
	public Cached<T> cache() {
		return new Cached<T>(this);
	}

}
