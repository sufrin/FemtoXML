package femtoXML;

/**
 * Represents the attributes of an XML element.
 * 
 * @author sufrin ($Revision$)
 * 
 */
public interface XMLAttributes
{
  /**
   * Associate the given attribute <code>value</code> with the given
   * <code>key</code>. If the key takes the form <tt><b>xmlns:</b>prefix</tt>
   * then add <tt>prefix</tt> to the namespace mapping of this scope. If the
   * key is <tt><b>xmlns</b></tt> then set the default namespace of
   * the current scope to <tt>value</tt> (a key of <b><tt>xmlns:xmlns</tt></b>
   * has the same effect).
   */
  String put(String key, String value);

  /**
   * Get the attribute value associated with this key.
   */
  String get(String key);

  /**
   * Print these attributes to the given <code>PrintStream</code>; use the
   * given indentation if the attributes get split across lines.
   */
  void printTo(FormatWriter out, int indent);
  
  /** Link these attributes into the attributes defined by a surrounding scope. */
  void setEnclosingScope(XMLAttributes attrs);
  
  /** Get the URN that corresponds to the given prefix in the current or a surrounding scope; null if there isn't one.
  */
  String getNameSpace(String prefix);
  
  /** Put the resolved namespace identifier of into the attribute mapping.
  */
  public String putNameSpace(String value);
  

  
  public XMLAttributes copy();
}

