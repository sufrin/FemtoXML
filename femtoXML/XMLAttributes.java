package femtoXML;
/**
 * Represents the attributes of an XML element.
 * @author sufrin
 *
 */
public interface XMLAttributes
{ /** Associate the given attribute <code>value</code> with the given <code>key</code> */
  String put(String key, String value);
  
  /** Get the attribute value associated with this key. 
   *  (The key is always a String. This signature is only because of an eccentric typing decision in the <code>java.util.Map</code> interface) */
  String get(Object key);
  
  /** Print these attributes to the given <code>PrintStream</code>; use the given 
   *  indentation if the attributes get split across lines.
   */
  void printTo(FormatWriter out, int indent);
}
