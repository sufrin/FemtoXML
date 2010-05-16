package femtoXML;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * An implementation of <code>XMLAttributes</code> that prints the attributes
 * of entitities in re-readable form.
 * 
 * @author sufrin ($Revision$)
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
  
  /** Make a fresh copy with identical attributes */
  public XMLAttributes copy()
  { XMLAttrMap r = new XMLAttrMap();
    r.split = split;
    r.expandedEntities = expandedEntities;
    r.split = split;
    for (String s : keySet()) r.put(s, get(s));
	return r;
    
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
  
  /** Add key=value to the mapping; if key is an xmlns:prefix then add prefix=value to the prefix mapping. 
   *  The namespace key "xmlns" maps to the current default namespace
   */
  public String put(String key, String value)
  { if (key.equals("xmlns")) 
    { if (nameSpaceMapping==null) nameSpaceMapping=new LinkedHashMap<String, String>();
      nameSpaceMapping.put("xmlns", value);
    }
    else if (key.startsWith("xmlns:")) 
    { if (nameSpaceMapping==null) nameSpaceMapping=new LinkedHashMap<String, String>();
      nameSpaceMapping.put(key.substring(6), value.intern());
    }

    return super.put(key, value); // superficial value
  }
  
  /** return the URN associated with the given prefix name; null if there isn't one. */
  public String getNameSpace(String prefixName)
  { 
	if (nameSpaceMapping!=null) 
    { String r = nameSpaceMapping.get(prefixName); 
      if (r!=null) return r;
    }
	// if (enclosingScope!=null) System.err.println("enclosing scope: "+enclosingScope.toString()); //**
    return enclosingScope==null ? null : enclosingScope.getNameSpace(prefixName);
  }
 
  protected XMLAttributes getEnclosingScope()
  {  	
	return enclosingScope;
  }

/** Construct an XMLAttrMap with default properties */
  public XMLAttrMap()
  {
  }
  
  /** Mapping from prefix to URI, null represents empty */
  protected HashMap<String, String> nameSpaceMapping = null;
  
  /** Parent mapping (for prefix scoping) */
  protected XMLAttributes enclosingScope = null;
  
  /** Set the parent mapping (for prefix scoping)*/
  public void setEnclosingScope(XMLAttributes parent)
  {
    this.enclosingScope = parent;
  }

  public String toString()
  {
    StringWriter sw = new StringWriter();
    FormatWriter out = new FormatWriter(sw);
    printTo(out, 0);
    out.flush();
    if (nameSpaceMapping!=null) out.print(" xmlnamespacemapping='"+nameSpaceMapping.toString()+"'"); //**
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
    // if (nameSpaceMapping!=null) out.print(" xmlnamespacemapping='"+nameSpaceMapping.toString()+"'"); //**
  }
}