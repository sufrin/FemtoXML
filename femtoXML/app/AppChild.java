package femtoXML.app;

/**
 * Abstract precursor of all AppTree implementations
 * @author sufrin
 *
 */

public abstract class AppChild implements AppTree
{
  protected AppElement parent;

  public void setParent(AppElement parent)
  {
    this.parent = parent;
  }

  public AppElement getParent()
  {
    return parent;
  }
  
  /** Returns an iterator that yields the path back to the root (as Trees) */
  
  public AppIterator<AppTree> toRoot()
  { return new AppIterator<AppTree>()
    { AppChild cursor = AppChild.this;
      public boolean hasNext()
      { return cursor !=null; 
      } 
      public AppTree next()
      { AppTree result = cursor;
        cursor = cursor.getParent();
        return result;
      }
    };
  }
  
  /** Returns an iterator that yields the path back to the root (as the names of elements) 
      with "" for a leaf.
  */
  public AppIterator<String> pathToRoot()
  { return new AppIterator<String>()
    { AppChild cursor = AppChild.this;
      public boolean hasNext()
      { return cursor !=null; 
      } 
      public String next()
      { String result = 
         (cursor instanceof AppElement) ? ((AppElement) cursor).getKind() : "";
        cursor = cursor.getParent();
        return result;
      }
    };
  }
}

