package femtoXML;

import java.io.PrintWriter;
/**
 * Represents the attributes of an XML element.
 * @author sufrin
 *
 */
public interface XMLAttributes
{ /** Associate the given attribute <code>value</code> with the given <code>key</code> */
  String put(String key, String value);
  
  /** The key is always a String. 
   *  (This signature only because of an eccentric typing decision in the <code>java.util.Map</code> interface) */
  String get(Object key);
  
  /** Print these attributes to the given <code>PrintStream</code> with the given indentation; if 
   * there is more than one attribute, then start a new line for each, and use the
   * given indentation.
   */
  void printTo(FormatWriter out, int indent);
}
