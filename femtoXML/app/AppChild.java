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
}
