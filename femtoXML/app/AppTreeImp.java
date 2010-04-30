package femtoXML.app;

/**
 * Abstract precursor of all AppTree implementations
 * 
 * @author sufrin
 * 
 */

public abstract class AppTreeImp implements AppTree, Iterable<AppTree> {
	protected AppElement parent;

	public void setParent(AppElement parent) {
		this.parent = parent;
	}

	public AppElement getParent() {
		return parent;
	}

	protected AppIterator<AppTree> Nil = new AppIterator.Nil<AppTree>();

	public AppIterator<AppTree> iterator() {
		return Nil;
	}

	/** Most trees are not elements: this yields false */
	public boolean isElement() {
		return false;
	}

	/** Most trees are not content: this yields false */
	public boolean isContent() {
		return false;
	}

	public String elementName() {
		return "-";
	}

	/** Returns a prefix order depth-first iterator */
	public AppIterator<AppTree> prefixIterator() {
		return prefixIterator(null);
	}
	
	
	/** Returns a prefix order depth-first iterator cutting off below <code>pred</code> */
	public AppIterator<AppTree> prefixIterator(AppPred pred) {
		return prefixIterator(this, pred);
	}

	/** Returns a breadth-first order iterator */
	public AppIterator<AppTree> breadthIterator() {
		return breadthIterator(this);
	}

	/** Returns an iterator that yields the path back to the root (as Trees) */

	public AppIterator<AppTree> pathToRoot() {
		final AppTree here = this;
		return new AppIterator<AppTree>() {
			AppTree cursor = here;

			public boolean hasNext() {
				return cursor != null;
			}

			public AppTree next() {
				AppTree result = cursor;
				cursor = cursor.getParent();
				return result;
			}
		};
	}

	/** Returns a prefix order depth-first iterator */
	public static AppIterator<AppTree> prefixIterator(final AppTree here, Pred<AppTree> p) {
		return new AppIterator<AppTree>() {
			/** Acts as a stack */
			AppIterator<AppTree> agenda = new AppIterator.Unit<AppTree>(here);

			public boolean hasNext() {
				return agenda.hasNext();
			}

			public AppTree next() {
				assert (hasNext());
				AppTree result = agenda.next();
				// Push the subtrees onto the stack
				if (result.isElement())
					agenda = new AppIterator.Cat<AppTree>(result.iterator(),
							agenda);
				return result;
			}
		};
	}

	/** Returns a breadth-first order iterator */
	public static AppIterator<AppTree> breadthIterator(final AppTree here) {
		return new AppIterator<AppTree>() {
			/** Acts as a queue */
			AppIterator<AppTree> agenda = new AppIterator.Unit<AppTree>(here);

			public boolean hasNext() {
				return agenda.hasNext();
			}

			public AppTree next() {
				assert (hasNext());
				AppTree result = agenda.next();
				// Queue the subtrees
				if (result.isElement())
					agenda = new AppIterator.Cat<AppTree>(agenda, result
							.iterator());
				return result;
			}
		};
	}

}
