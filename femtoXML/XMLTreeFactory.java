package femtoXML;

public interface XMLTreeFactory<Tree>
{
  XMLComposite<Tree> newElement(String kind, XMLAttrs atts);

  XMLComposite<Tree> newRoot();

  Tree newWord(String text, boolean cdata);

  Tree newComment(String text);

  Tree newPI(String text);

  boolean canComment();
  boolean canPI();
}
