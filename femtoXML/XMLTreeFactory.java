package femtoXML;

import java.util.*;

public interface XMLTreeFactory<Tree>
{
  XMLComposite<Tree> newElement(String kind, Map<String, String> atts);

  XMLComposite<Tree> newRoot();

  Tree newWord(String text, boolean cdata);

  Tree newComment(String text);

  Tree newPI(String text);

  boolean canComment();
  boolean canPI();
}
