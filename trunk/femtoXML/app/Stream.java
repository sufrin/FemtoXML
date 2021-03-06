package femtoXML.app;

import java.util.Iterator;
import static java.util.Arrays.*;
import java.util.*;

/**
 * $Id$
 * 
 * General purpose toolkit for working with iterator-like structures.
 * A <code>Stream</code> is an iterator with a few derived methods. It
 * doesn't implement <code>remove</code>.
 * 
 */
public abstract class Stream<T> implements Iterator<T>, Iterable<T>
{
        /** Class that implements catenation */
        public static class Cat<T> extends Stream<T>
        {
                Stream<T> a, b, t;

                // INV: t==a || !a.hasNext && t==b || !(a.hasNext ||
                // b.hasNext) &&
                // t==null

                public Cat(Stream<T> a, Stream<T> b)
                {
                        this.a = a;
                        this.b = b;
                        this.t = a;
                }

                public Stream<T> copy()
                {
                        return new Cat<T>(a.copy(), b.copy());
                }

                public boolean hasNext()
                {
                        // return a.hasNext() || b.hasNext();
                        while (t != null)
                                if (t.hasNext())
                                        return true;
                                else
                                        t = (t == a ? b : null);
                        return false;
                }

                public T next()
                {
                        return t.next();
                }
        }
        
        public static class Map<T,U> extends Stream<U>
        {
                Stream<T> base;
                Expr<T,U> expr;
                public Map(Stream<T> base, Expr<T,U> expr) {this.base=base; this.expr = expr; }
                
                public boolean hasNext() { return base.hasNext(); }
                public U next() { return expr.eval(base.next()); }
                public Stream<U> copy() { return new Map<T,U>(base, expr); } 
        }
        
        public static <T> Stream<T> concat(Stream<Stream<T>> cursors) { return new Concat<T>(cursors); }
        public static <T> Stream<T> flatten(Stream<Stream<T>> cursors) { return new Concat<T>(cursors); }
        
        /** Catenate the cursors from a stream of cursors. */
        public static class Concat<T> extends Stream<T>
        {
                Stream<Stream<T>> sources;
                Stream<T>         current = null;
                
                public Concat(Stream<Stream<T>> sources) { this.sources=sources; if (sources.hasNext()) current=sources.next(); }
                
                public boolean hasNext()
                {
                        while (current!=null)
                                  if (current.hasNext()) 
                                          return true;
                                  else
                                          current=sources.hasNext() ? sources.next() : null;
                   // current == null
                   return false;
                }
                
                public Stream<T> copy() { return new Concat<T>(sources.copy()); }
                
                public T next()
                {
                        return current.next();
                }
                
        }

        /**
         * abstract class that implements filtering; needs
         * <code>pass</code> to be defined.
         */
        public static abstract class Filter<T> extends Stream<T>
        {
                /** Base stream being filtered */
                Stream<T> base;
                /**
                 * INV: cache!=null => pass(cache) cache==null =>
                 * !base.hasNext()
                 */
                T cache;

                public Filter(Stream<T> base)
                {
                        this.base = base;
                        cache = null;
                        seek();
                }

                public Stream<T> copy()
                {
                        return new Filter<T>(base.copy())
                        {
                                public boolean pass(T t)
                                {
                                        return Filter.this.pass(t);
                                }
                        };
                }

                public boolean hasNext()
                {
                        return cache != null;
                }

                public T next()
                {
                        T result = cache;
                        seek();
                        return result;
                }

                /**
                 * predicate to be satisfied by the elements from the base
                 * stream in order to be admitted to the filtered stream.
                 */
                abstract public boolean pass(T t);

                /** (Re) establish the invariant */
                void seek()
                {
                        cache = null;
                        while (base.hasNext() && cache == null)
                        {
                                cache = base.next();
                                if (!pass(cache))
                                        cache = null;
                        }
                }
        }

        /** Generate an empty cursor */
        public static class Nil<T> extends Stream<T>
        {
                public Stream<T> copy()
                {
                        return this;
                }

                public boolean hasNext()
                {
                        return false;
                }

                public T next()
                {
                        throw new IllegalStateException();
                }
        }

        /** Generate a unit cursor */
        public static class Unit<T> extends Stream<T>
        {
                T t;
                boolean read;

                public Unit(T t)
                {
                        this.t = t;
                        read = false;
                }

                public Stream<T> copy()
                {
                        return new Unit<T>(t);
                }

                public boolean hasNext()
                {
                        return !read;
                }

                public T next()
                {
                        if (read)
                                throw new IllegalStateException();
                        else
                        {
                                read = true;
                                return t;
                        }
                }
        }

        /** Construct a <code>Stream</code> from an <code>Iterable</code> */
        public static <T> Stream<T> appIterator(final Iterable<T> iterable)
        {
                final Iterator<T> it = iterable.iterator();
                return new Stream<T>()
                {
                        public Stream<T> copy()
                        {
                                return Stream.appIterator(iterable);
                        }

                        public boolean hasNext()
                        {
                                return it.hasNext();
                        }

                        public T next()
                        {
                                return it.next();
                        }
                };
        }
        
        /** Construct a <code>Vector</code> whose elements are the (remaining) elements of this cursor. */
        public java.util.Vector<T> vector()
        {
                java.util.Vector<T> r = new java.util.Vector<T>();
                while (hasNext()) r.add(next());
                return r;
        }
        

        // cursory unit test
        public static void main(String[] args)
        {
                List<String> l = asList(args);
                Stream<String> a = appIterator(l);
                Stream<String> b = appIterator(l);
                Pred<String> p = new Pred<String>()
                {
                        public boolean pass(String s)
                        {
                                return s.endsWith(".class");
                        }
                };
                for (String s : a.cat(b).filter(p))
                        System.err.println(s);
        }

        /** Catenate this cursor with another */
        public Stream<T> cat(Stream<T> other)
        {
                return new Cat<T>(this, other);
        }
        
        /** Catenate this cursor with a singleton */
        public Stream<T> cat(T other)
        {
                return new Cat<T>(this, new Unit<T>(other));
        }
        
        /** Map an expression over the elements of this cursor */
        public <U> Map<T,U> map(Expr<T,U> expr) { return new Map<T,U>(this, expr); }

        /** Hint that resources tied up in the cursor may be discarded */
        public void close()
        {
        }

        public abstract Stream<T> copy();

        /** Drop the next n elements from this cursor */
        public Stream<T> drop(int n)
        {
                for (int i = 0; i < n; hasNext(), i++)
                        next();
                return this;
        }

        /** Generate a filtered variant of this iterator */
        public Stream<T> filter(final Pred<T> p)
        {
                return new Filter<T>(this)
                {
                        public boolean pass(T t)
                        {
                                return p.pass(t);
                        }
                };
        }

        /** Interface to fancy for loops */
        public Iterator<T> iterator()
        {
                return this;
        }

        /** Unimplemented */
        public void remove()
        {
                throw new UnsupportedOperationException();
        }

        /**
         * Generate a new cursor which yields the first n elements of this
         * cursor, and then stops
         */
        public Stream<T> take(final int n)
        {
                final Stream<T> base = this;
                return new Stream<T>()
                {
                        int left = n;

                        public Stream<T> copy()
                        {
                                return base.copy();
                        }

                        public boolean hasNext()
                        {
                                return left > 0 && base.hasNext();
                        }

                        public T next()
                        {
                                assert (hasNext());
                                left--;
                                return base.next();
                        }
                };
        }

}
