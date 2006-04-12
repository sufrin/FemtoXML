package femtoXML;

/**
 * An <code>XMLComposiite</code> is the abstraction of a node of a parse tree.
 * 
 * @author sufrin
 *
 * @param <Tree> -- the type of the parse tree
 */
public interface XMLComposite<Tree>
{ /** Add an (inner) node to the composite. */
  void addTree(Tree subtree);
  
  /**
   * Transform an open composite into a closed one. This is used to signal to a
   * composite that it is complete. The composite may choose a different
   * representation at this point, or may simply return itself as
   * <code>this</code>.
   */
  Tree close();
  
  /** Is this node interested in the spacing of the original content? */
  boolean wantSpaces();
}
