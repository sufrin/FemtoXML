package femtoXML.app;

/**
 * Abstract precursor of all AppTree implementations
 * @author sufrin
 *
 */

public abstract class AppTreeImp implements AppTree, Iterable<AppTree>
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
  
  protected  AppIterator<AppTree> Nil = new AppIterator.Nil<AppTree>();
  
  public AppIterator<AppTree> iterator() 
  { return Nil;
  } 
  
  /** Most trees are not elements */
  public boolean isElement() { return false; }
  
  
}

