package femtoXML.app;

import femtoXML.FormatWriter;


public interface AppTree
{
  void    printTo(FormatWriter out, int indent);
  boolean isWord();
}
