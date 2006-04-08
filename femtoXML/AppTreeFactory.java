package femtoXML;

import java.util.*;

public class AppTreeFactory implements XMLTreeFactory<AppTree>
{ protected boolean expandEntities = true;
  
  public AppTreeFactory(boolean expandEntities) { this.expandEntities = expandEntities; }
  
  public AppTreeFactory() { this(true); }
  
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
    return new AppWord(name, cdata, expandEntities);
  }

  public AppTree newComment(String data)
  {
    return new AppComment(data);
  }

  public AppTree newPI(String data)
  {
    return new AppPI(data);
  }

  public boolean canComment()
  {
    return true;
  }
  
  public boolean canPI()
  {
    return true;
  }
}

