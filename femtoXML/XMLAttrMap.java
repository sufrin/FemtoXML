package femtoXML;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * An implementation of <code>XMLAttributes</code> that prints the attributes
 * of entitities in re-readable form.
 */
@SuppressWarnings("serial")
public class XMLAttrMap extends LinkedHashMap<String, String> implements
    XMLAttributes
{
  /** True if entitities were expanded in attribute values when they were read. */
  protected boolean expandedEntities;
  
  /** True if entitities were expanded in attribute values when they were read. */
  public boolean getExpandedEntities() { return expandedEntities; }
  
  public XMLAttrMap setExpandedEntities(boolean expandedEntities)
  {
	  this.expandedEntities = expandedEntities;
	  return this;
  }

  /**
   * Largest number of key=value pairs before this is split across several lines
   * when printed
   */
  protected int     split = 2;

  /**
   * Set the largest number of key=value pairs before this will be split across
   * several lines when printed
   */
  public XMLAttrMap setSplit(int split)
  {
    this.split = split;
    return this;
  }

  /**
   * Current largest number of key=value pairs before this will be split across
   * several lines when printed
   */
  public int getSplit()
  {
    return split;
  }

  public String get(String key)
  {
    return super.get(key);
  }

 
  /** Construct an XMLAttrMap with default properties */
  public XMLAttrMap()
  {
  }

  public String toString()
  {
    StringWriter sw = new StringWriter();
    FormatWriter out = new FormatWriter(sw);
    printTo(out, 0);
    out.flush();
    return sw.toString();
  }
  
  protected boolean align = true;
  
  public XMLAttrMap setAlign(boolean align) 
  { this.align = align; return this; }

  /**
   * Print in re-readable form on the given FormatWriter, using the given indent
   * if the printed form is split across lines.
   */
  public void printTo(FormatWriter out, int indent)
  {
    Set<String> keys = keySet();
    boolean indenting = keys.size() > split && indent > 0;
    int w = 0;
    if (indenting && align) for (String key : keys) w = Math.max(w, key.length());
    for (String key : keys)
    {
      String val = get(key);
      char quote = val.indexOf('"') == -1 ? '"' : '\'';
      if (indenting)
      {
        out.println();
        out.indent(indent);
      }
      else
        out.print(" ");
      out.print(key);
      if (indenting && align) for (int i=key.length(); i<w; i++) out.print(" ");
      out.print(" = ");
      out.print(quote);
      if (expandedEntities)
        XMLCharUtil.print(out, val);
      else
        out.print(val);
      out.print(quote);
    }
  }
}