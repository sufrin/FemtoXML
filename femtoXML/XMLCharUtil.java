package femtoXML;

import femtoXML.FormatWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

/**
 * Static utilities to support input and output of XML character entitities.
 * 
 * @author sufrin
 *
 */
public class XMLCharUtil
{ static Map<String,Character>  toChar   = new Hashtable<String,Character>();
  static Map<Character, String> fromChar = new Hashtable<Character, String>();
  
  public static void newEntity(String name, char character)
  {
    Character ch = new Character(character);
    toChar.put(name, ch);
    fromChar.put(ch, name);
  }
  
  static
  {
    newEntity("Aacute",'\u00C1');
    newEntity("aacute",'\u00E1');
  }
  
  /** Re-quote special characters as entitities within the given string. */
  public static String unQuote(String s)
  {
    StringWriter quoted = new StringWriter();
    FormatWriter out = new FormatWriter(quoted);
    print(out, s);
    out.flush();
    return quoted.toString();
  }
  
  /** Print the given string on the given writer, representing special characters by named entitities. */
  public static void print(FormatWriter out, String s)
  {
    for (int i=0; i<s.length(); i++) print(out, s.charAt(i));
  }
  
  /** Print the given character on the given writer, representing special characters by named entitities
   *  if the writer's isAscii property is set.  
   */
  public static void print(FormatWriter out, char c)
  { switch (c)
    {
      case '<':      out.print("&lt;");   break;
      case '>':      out.print("&gt;");   break;
      case '&':      out.print("&amp;");  break;
      case '"':      out.print("&quot;"); break;
      case '\'':     out.print("&apos;"); break;
      case '\u00A0': out.print("&nbsp;"); break;
      default:   
        if (out.getCharEntities())
        {
          String name = fromChar.get(c);
          if (name==null)
            if (c>128) out.print(String.format("&#x%X;", (int) c)); else out.print(c);
          else
            out.print(String.format("&%s;", name));
        }
        else
          out.print(c);
    }
  }
  
  /** Decode the given character entity name, returning 0 if the name is unknown. */
  public static char decodeCharEntity(String entityName)
  { if      ("amp".equals(entityName))  return  '&'; 
    else if ("apos".equals(entityName)) return  '\'';    
    else if ("gt".equals(entityName))   return  '>';
    else if ("lt".equals(entityName))   return  '<';
    else if ("quot".equals(entityName)) return  '"';
    else if ("nbsp".equals(entityName)) return  '\u00A0';
    else if (entityName.matches("#[Xx][0-9]+")) return  (char) Integer.parseInt(entityName.substring(2), 16);
    else if (entityName.matches("#[0-9]+"))     return  (char) Integer.parseInt(entityName.substring(1), 10);
    else
    { Character result = toChar.get(entityName);
      if (result==null) return  (char) 0; else return result;      
    }
  }

}
