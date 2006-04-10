package femtoXML;

public interface XMLTreeFactory<Tree>
{
  XMLComposite<Tree> newElement(String kind, XMLAttributes atts);

  XMLComposite<Tree> newRoot();

  Tree newWord(String text, boolean cdata);
  
  /** Generate a tree node for the comment whose body is the given text */
  Tree newComment(String text);

  /** Generate a tree node for the PI whose body is the given text */
  Tree newPI(String text);  
  
  /** Generate a tree node for the DOCTYPE whose body is the given text */
  Tree newDOCTYPE(String text);
  
  /** Generate a tree node to represent non-markup space */
  Tree newSpaces(String text);

  /** Return true if newComment can legitimately be called  */
  boolean canComment();
  
  /** Return true if newPI can legitimately be called  */
  boolean canPI();  
  
  /** Return true if newDOCTYPE can legitimately be called  */
  boolean canDOCTYPE();
  
  /** Return true if the named element wants to record spaces */
  boolean wantSpaces(String elementKind);
}
