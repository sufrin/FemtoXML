package femtoXML.app;
import java.util.Iterator;
import static java.util.Arrays.*;
import java.util.*;

/**
        General purpose toolkit for working with iterator-like
        structures. An <code>AppIterator</code> is an iterator with
        a few derived methods. It doesn't implement <code>remove</code>.
*/
public abstract class AppIterator<T> implements Iterator<T>, Iterable<T>
{ /** Unimplemented */
  public void remove() { throw new UnsupportedOperationException(); }
  
  /** Hint that resources tied up in the iterator may be discarded */
  public void close() { }
  
  /** Interface tofacny for loops */
  public Iterator<T> iterator() { return this; }

  /** Catenate this iterator to another */
  public AppIterator<T> cat(Iterator<T> other) { return new Cat<T>(this, other); } 
  
  /** Generate a filtered variant of this iterator */
  public AppIterator<T> filter(final Pred<T> p) 
  { return new Filter<T>(this)
    { public boolean pass(T t) { return p.pass(t); }
    }; 
  } 
  
  /** The empty iterator */
  public static class Nil<T> extends AppIterator<T>
  { public boolean hasNext() { return false; }
    public T next() { throw new IllegalStateException(); }
  }
  
  /** The unit iterator */
  public static class Unit<T> extends AppIterator<T>
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
  public static class Cat<T> extends AppIterator<T>
  { Iterator<T> a, b;
    public Cat(Iterator<T> a, Iterator<T> b) { this.a=a; this.b=b; }
    public boolean hasNext() { return a.hasNext() || b.hasNext(); }
    public T next() 
    {  if (a.hasNext()) return a.next(); else return b.next(); 
    }
  }
  
  /** abstract class that implements filtering; needs <code>pass</code> to be defined. */
  public static abstract class Filter<T> extends AppIterator<T>
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
  
  /** Coerce an <code>Iterator</code> into an <code>AppIterator</code> */
  public static <T> AppIterator<T> appIterator(final Iterator<T> t)
  { return new AppIterator<T>()
    { public boolean hasNext() { return t.hasNext(); }
      public T next()          { return t.next(); }
    };
  }

  /** Construct an  <code>AppIterator</code> from an <code>Iterable</code>*/
  public static <T> AppIterator<T> appIterator(Iterable<T> it)
  { return appIterator(it.iterator()); }
  
  
  // cursory unit test
  public static void main(String[] args)
  { List<String>        l = asList(args);
    AppIterator<String> a = appIterator(l);
    AppIterator<String> b = appIterator(l);
    Pred<String> p = new Pred<String>()
    { public boolean pass(String s)
      { return s.endsWith(".class"); }
    };
    for (String s : a.cat(b).filter(p)) System.err.println(s);
  }

}