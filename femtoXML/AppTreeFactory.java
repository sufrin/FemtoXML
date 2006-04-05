package femtoXML;

import java.util.*;

public class AppTreeFactory implements XMLTreeFactory<AppTree>
{
  public AppElement newElement(String kind, Map<String, String> atts)
  {
    return new AppElement(kind, atts);
  }

  public AppElement newRoot()
  {
    return newElement("", null);
  }

  public AppWord newWord(String name, boolean cdata)
  {
    return new AppWord(name, cdata);
  }

  public AppTree newComment(String data)
  {
    return null;
  }

  public AppTree newPI(String data)
  {
    return null;
  }

  public boolean canComment()
  {
    return false;
  }
  
  public boolean canPI()
  {
    return false;
  }
}
