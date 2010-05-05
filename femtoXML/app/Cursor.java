package femtoXML.app;
import java.util.Iterator;
import static java.util.Arrays.*;
import java.util.*;

/**
        General purpose toolkit for working with iterator-like
        structures. A <code>Cursor</code> is an iterator with
        a few derived methods. It doesn't implement <code>remove</code>.
*/
public abstract class Cursor<T> implements Iterator<T>, Iterable<T>
{ /** Unimplemented */
  public void remove() { throw new UnsupportedOperationException(); }
  
  /** Hint that resources tied up in the iterator may be discarded */
  public void close() { }
  
  /** Interface to fancy for loops */
  public Iterator<T> iterator() { return this; }
  
  public Cursor<T> drop(int n) 
  {   for (int i=0; i<n; hasNext(), i++) next();
	  return this;
  }

  /** Catenate this iterator to another */
  public Cursor<T> cat(Iterator<T> other) { return new Cat<T>(this, other); } 
  
  /** Generate a filtered variant of this iterator */
  public Cursor<T> filter(final Pred<T> p) 
  { return new Filter<T>(this)
    { public boolean pass(T t) { return p.pass(t); }
    }; 
  } 
  
  /** The empty iterator */
  public static class Nil<T> extends Cursor<T>
  { public boolean hasNext() { return false; }
    public T next() { throw new IllegalStateException(); }
  }
  
  /** The unit iterator */
  public static class Unit<T> extends Cursor<T>
  { T t;
    boolean read;
    public Unit(T t) { this.t = t; read = false; }
    
    public boolean hasNext() { return !read; }
    public T next() 
    { if (read) 
         throw new IllegalStateException(); 
      else
         { read = true; 
           return t;
         }
    }
  }
  
  /** Class that implements catenation */
  public static class Cat<T> extends Cursor<T>
  { Iterator<T> a, b, t;
    // INV: t==a || !a.hasNext && t==b || !(a.hasNext || b.hasNext) && t==null
  
    public Cat(Iterator<T> a, Iterator<T> b) { this.a=a; this.b=b; this.t=a; }
    
    public boolean hasNext() { 
    	// return a.hasNext() || b.hasNext(); 
    	while (t!=null)
    	      if (t.hasNext()) return true; else t = (t==a?b:null);
    	return false;
    }
    
    public T next() 
    {  return t.next(); 
    }
  }
  
  /** abstract class that implements filtering; needs <code>pass</code> to be defined. */
  public static abstract class Filter<T> extends Cursor<T>
  { Iterator<T> a;
    T           t; 
    // INV: t==null || pass(t)
    
    abstract public boolean pass(T t);
    
    public Filter(Iterator<T> a) { this.a=a; t=null; seek(); }
    
    void seek() 
    { t=null;
      while (a.hasNext() && t==null) 
      { t = a.next();
        if (!pass(t)) t=null;
      }
      // !a.hasNext() || (t!=null && pass(t))
    }
    
    public boolean hasNext() { return t!=null;  }
    
    public T next() 
    {  T result = t;
       seek();
       return result;
    }
  }
  
  /** Coerce an <code>Iterator</code> into an <code>Cursor</code> */
  public static <T> Cursor<T> cursor(final Iterator<T> t)
  { return new Cursor<T>()
    { public boolean hasNext() { return t.hasNext(); }
      public T next()          { return t.next(); }
    };
  }

  /** Construct an  <code>Cursor</code> from an <code>Iterable</code>*/
  public static <T> Cursor<T> appIterator(Iterable<T> it)
  { return cursor(it.iterator()); }
    
  // cursory unit test
  public static void main(String[] args)
  { List<String>        l = asList(args);
    Cursor<String> a = appIterator(l);
    Cursor<String> b = appIterator(l);
    Pred<String> p = new Pred<String>()
    { public boolean pass(String s)
      { return s.endsWith(".class"); }
    };
    for (String s : a.cat(b).filter(p)) System.err.println(s);
  }

}
