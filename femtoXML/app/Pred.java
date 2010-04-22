package femtoXML.app;

/**
    Predicates and their algebra
*/
public abstract class Pred<T>
{ abstract public boolean pass(T t);
  
  public Pred<T> and(final Pred<T> other)
  { return new Pred<T>()
    { public boolean pass(T t) { return Pred.this.pass(t) && other.pass(t); }
    };
  }
  public Pred<T> or(final Pred<T> other)
  { return new Pred<T>()
    { public boolean pass(T t) { return Pred.this.pass(t) || other.pass(t); }
    };
  }
  public Pred<T> not()
  { return new Pred<T>()
    { public boolean pass(T t) { return !Pred.this.pass(t);}
    };
  }
  public static<T> Pred<T> TRUE()
  { return new Pred<T>()
    { public boolean pass(T t) { return true;}
    };
  }
  public static<T> Pred<T> FALSE()
  { return new Pred<T>()
    { public boolean pass(T t) { return false;}
    };
  } 
}
