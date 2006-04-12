package femtoXML;

import femtoXML.FormatWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

/**
 * Static utilities to support input and output of XML strings. This provides
 * translation to and from character entity names, and output of strings with or
 * without translation of special characters to character entitities.
 * 
 * @author sufrin
 * 
 */
public class XMLCharUtil
{ private static final char    
  QUOT = 0x22, 
  APOS = 0x27;

  static Map<String, Character> toChar   = new Hashtable<String, Character>();

  static Map<Character, String> fromChar = new Hashtable<Character, String>();

  /**
   * Augment the character-entity name table by naming the given character.
   */
  public static void newEntity(String name, char character)
  {
    Character ch = new Character(character);
    toChar.put(name, ch);
    fromChar.put(ch, name);
  }

  static
  {
    newEntity("nbsp",   '\u00A0');
    newEntity("Aacute", '\u00C1');
    newEntity("aacute", '\u00E1');
    newEntity("copy",   '\u00A9');
    newEntity("reg",    '\u00AE');    
    newEntity("pound",  '\u00A3');    
    newEntity("sect",   '\u00A7');    
    newEntity("para",   '\u00B6');    
    newEntity("middot", '\u00B7');  
    newEntity("laquo",  '\u00AB');  
    newEntity("raquo",  '\u00BB');  
    newEntity("ldquo",  '\u201C');  
    newEntity("rdquo",  '\u201D');  
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

  /**
   * Print the given string on the given writer, representing special characters
   * by named entitities.
   */
  public static void print(FormatWriter out, String s)
  {
    for (int i = 0; i < s.length(); i++)
      print(out, s.charAt(i));
  }

  /**
   * Print the given character on the given writer, representing special
   * characters (those with Unicode encodings >= 128) by named or numeric
   * character entitities if the writer's <code>charEntities</code> property
   * is set.
   */
  public static void print(FormatWriter out, char c)
  {
    switch (c)
    {
      case '<':
        out.print("&lt;");
        break;
      case '>':
        out.print("&gt;");
        break;
      case '&':
        out.print("&amp;");
        break;
      case QUOT:
        out.print("&quot;");
        break;
      case APOS:
        out.print("&apos;");
        break;
      default:
        if (out.getCharEntities())
        {
          String name = fromChar.get(c);
          if (name == null)
            if (c >= 128)
              out.print(String.format("&#x%X;", (int) c));
            else
              out.print(c);
          else
            out.print(String.format("&%s;", name));
        }
        else
          out.print(c);
    }
  }

  /** Decode the given character entity name, returning 0 if the name is unknown. */
  public static char decodeCharEntity(String entityName)
  {
    if ("amp".equals(entityName))
      return '&';
    else if ("apos".equals(entityName))
      return APOS;
    else if ("gt".equals(entityName))
      return '>';
    else if ("lt".equals(entityName))
      return '<';
    else if ("quot".equals(entityName))
      return QUOT;
    else if (entityName.matches("#[Xx][0-9a-fA-F]+"))
      return (char) Integer.parseInt(entityName.substring(2), 16);
    else if (entityName.matches("#[0-9]+"))
      return (char) Integer.parseInt(entityName.substring(1), 10);
    else
    {
      Character result = toChar.get(entityName);
      if (result == null)
        return (char) 0;
      else
        return result;
    }
  }
}
