package femtoXML;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * An <code>XMLScanner</code> is primed with an <code>XMLHandler</code>, and then can be used(and re-used) to read XML from a
 * <code>java.io.Reader</code>.
 * There are almost no limitations on the form of the XML it will read, <b>but</b>:
 * <ul>
 *  <li>No attempt is made to recover from XML parsing errors.</li>
 *  <li>DOCTYPE declarations are ignored</li>
 *  <li>Processing instructions are ignored</li>
 *  <li>Namespace prefixes are incorporated into entity names so namespace processing has to be performed further up the analysis chain</li>
 * </ul>
 * 
 * */
public class XMLScanner
{
  protected XMLHandler consumer;
  protected Reader     reader;
  protected int        lineCount,  // where the current character came from
                        lineNumber; // where the current symbol started

  protected void throwSyntaxError(String error) { throw new XMLSyntaxError(error, lineNumber); }

  public XMLScanner()
  {}

  public XMLScanner(XMLHandler consumer)
  {
    setConsumer(consumer);
  }

  public void setConsumer(XMLHandler consumer)
  {
    this.consumer = consumer;
  }

  /** Return the current source line number */
  public int lineNumber()
  {
    return lineNumber;
  }

  /** The current lexical symbol */
  protected Lex token = null;

  /** Check the current token is the given token and skip over it. */
  protected void skipToken(Lex token)
  {
    checkToken(token);
    nextToken();
  }

  /** Check the current token is the given token; fail if it isn't. */
  protected void checkToken(Lex token)
  {
    nextToken();
    if (this.token != token)
                            throwSyntaxError(token + " expected; found "
                                                + this.token + " " + this.value);
  }

  private enum Lex
  {
    ENDSTREAM("END-OF-XML-STREAM"), POINTBRA("<"), POINTKET(">"), POINTBRASLASH(
        "</"), SLASHPOINTKET("/>"), WORD("WORD"), IDENTIFIER("IDENTIFIER"), CDATA(
        "<![CDATA[ ..."), SQUOTE("' or \""), EQUALS("="), COMMENT("<!-- ..."), PROCESS(
        "<? ... ?>"), DOCTYPE("<!DOCTYPE ...");
    Lex(String name)
    {
      this.name = name;
    }

    private String name;

    public String toString()
    {
      return name;
    }
  }

  /**
   * Read XML from the given Reader, invoking the current <code>XMLHandler consumer</code>'s methods at
   * appropriate times and keeping track of line numbers. If a client needs to keep track of column
   * numbers as well then <code>nextRawChar()</code> should be overridden in a subclass.
   */
  public void read(Reader reader)
  {
    this.reader = reader;
    this.lineCount = 1;
    ch = 0;
    nextToken();
    consumer.startDocument();
    while (token != Lex.ENDSTREAM)
    {
      switch (token)
      {
        case DOCTYPE:
        case PROCESS:
          // Ignore DTDs and processing instructions
          break;
        case IDENTIFIER:
        case WORD:
        case CDATA:
          consumer.wordCharacters(value);
          break;
        case POINTBRASLASH: // </ tag >
          checkToken(Lex.IDENTIFIER);
          consumer.endElement(value);
          checkToken(Lex.POINTKET);
          break;
        case COMMENT: // <!-- ... -->
          consumer.commentCharacters(value);
          break;
        default:
          throwSyntaxError("Unexpected token: " + token + " " + value);
        case POINTBRA: // <id id="..." ...
        {
          Map<String, String> atts = new Attributes();
          inElement = true;
          checkToken(Lex.IDENTIFIER);
          String tag = value;
          nextToken();
          while (token == Lex.IDENTIFIER)
          {
            String key = value;
            skipToken(Lex.EQUALS);
            if (token == Lex.SQUOTE)
            {
              atts.put(key.intern(), value.intern());
              nextToken();
            }
            else
              throwSyntaxError("Found " + token
                                  + " when string expected in " + key + "=...");
          }
          consumer.startElement(tag, atts);
          if (token == Lex.SLASHPOINTKET) // />
            consumer.endElement(tag);
          else if (token != Lex.POINTKET)
                                         // >
                                         throwSyntaxError("> expected in start tag: found "
                                                             + token);
          inElement = false;
        }
      }
      nextToken();
    }
    consumer.endDocument();
    try
    {
      reader.close();
    }
    catch (Exception e)
    {}
  }

  /** The current symbol's characters, if it's a class */
  protected String  value     = null;

  /** The current character */
  protected int     ch;

  /** Expansion of the last entity read */
  protected String  entity;

  /** True iff currently reading an element header < ... /> or < ... > */
  protected boolean inElement = false;

  /** Read the next token */
  protected void nextToken()
  {
    if (0 <= ch && ch <= ' ')
    {
      do
      {
        nextRawChar();
      }
      while (0 <= ch && ch <= ' ');
    }
    lineNumber = lineCount;
    value = "";
    if (ch == -1)
    {
      token = Lex.ENDSTREAM;
    }
    else
    // ... substantive symbols ...
    if (inElement && ch == '=')
    {
      token = Lex.EQUALS;
      value = "";
      nextRawChar();
    }
    else if (inElement && (ch == '\'' || ch == '"'))
    {
      int close = ch;
      StringBuilder b = new StringBuilder();
      nextChar();
      while (0 <= ch && ch != close)
      {
        if (ch == '&')
          b.append(entity);
        else
          b.append((char) ch);
        nextChar();
      }
      token = Lex.SQUOTE;
      value = b.toString();
      nextRawChar();
    }
    else if (inElement && ch == '/')
    {
      nextRawChar();
      if (ch == '>')
      {
        nextRawChar();
        token = Lex.SLASHPOINTKET;
      }
      else
        throwSyntaxError("/> expected; found /" + ((char) ch));
    }
    else if (ch == '<')
    {
      nextRawChar();
      if (ch == '/')
      {
        nextRawChar();
        token = Lex.POINTBRASLASH;
      }
      else if (ch == '?')
      {
        nextRawChar();
        int lastch = ch;
        StringBuilder b = new StringBuilder();
        while (0 <= ch && !(ch == '>' && lastch == '?'))
        {
          b.append((char) ch);
          lastch = ch;
          nextRawChar();
        }
        if (ch == -1)
          throwSyntaxError("<? with runaway body ...");
        else
        {
          nextRawChar();
          value = b.substring(0, b.length() - 1);
          token = Lex.PROCESS;
        }
      }
      else if (ch == '!')
      {
        nextRawChar();
        if (ch == '[') // Assume <![CDATA and read to closing ]]>
        {
          StringBuilder b = new StringBuilder();
          do
          {
            while (0 <= ch && ch != '>')
            {
              b.append((char) ch);
              nextRawChar();
            }
            if (ch > 0)
            {
              b.append((char) ch);
              nextRawChar();
            }
          }
          while (0 <= ch && !endCDATA(b));
          if (ch < 0)
            throwSyntaxError("<![CDATA[ ... ]]> expected; found <!"
                                + b.toString() + " at end of file");
          else if (isCDATA(b))
          {
            value = b.substring(7, b.length() - 3);
            token = Lex.CDATA;
          }
          else
            throwSyntaxError("<![CDATA[ ... ]]> expected; found <!"
                                + b.toString() + ">");
        }
        else if (ch == 'D') // Assume <!DOCTYPE and read to matching closing >
        {
          int count = 1;
          while (0 <= ch && count > 0)
          {
            nextRawChar();
            if (ch == '<')
              count++;
            else if (ch == '>') count--;
          }
          if (count != 0)
                         throwSyntaxError("<!DOCTYPE with runaway body ...");
          token = Lex.DOCTYPE;
          nextRawChar();
        }
        else
        // Assume <!-- comment -->
        {
          StringBuilder b = new StringBuilder();
          do
          {
            while (0 <= ch && ch != '>')
            {
              b.append((char) ch);
              nextRawChar();
            }
            if (ch > 0)
            {
              b.append((char) ch);
              nextRawChar();
            }
          }
          while (0 <= ch && !endComment(b));
          if (isComment(b))
          {
            value = b.substring(2, b.length() - 3);
            token = Lex.COMMENT;
          }
          else
            throwSyntaxError("<!-- ... --> expected; found <!"
                                + b.toString());
        }
      }
      else
        token = Lex.POINTBRA;
    }
    else if (ch == '>')
    {
      nextRawChar();
      token = Lex.POINTKET;
    }
    else
    // a new ``word'' lump begins
    {
      StringBuilder b = new StringBuilder();
      token = Lex.IDENTIFIER;
      // leading & is a special case
      if (ch == '&')
      {
        token = Lex.WORD;
        nextEnt();
        b.append(entity);
        nextChar();
      }
      while (ch > ' ' && ch != '<' && ch != '>'
             && !(inElement && (ch == '/' || ch == '=')))
      {
        if (ch == '&')
          b.append(entity);
        else
          b.append((char) ch);
        if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != ':')
                                                                     token = Lex.WORD;
        nextChar();
      }
      value = b.toString();
    }
  }

  /** Read the next character -- expanding entities */
  protected void nextChar()
  {
    nextRawChar();
    if (ch == '&')
    {
      nextEnt();
      ch = '&';
    }
  }

  /** Read the next raw character, keeping track of the line number */
  protected void nextRawChar()
  {
    try
    {
      ch = reader.read();
      if (ch == '\n') lineCount++;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Read and expand the next entity; the variable 'entity' is set to the
   * expansion.
   */
  protected void nextEnt()
  {
    String ent = "";
    nextRawChar();
    while (' ' < ch && ch != ';')
    {
      ent = ent + ((char) ch);
      nextRawChar();
    }
    if (ent.equals("amp"))
      entity = "&";
    else if (ent.equals("ls"))
      entity = "<";
    else if (ent.equals("gr"))
      entity = ">";
    else if (ent.equals("apos"))
      entity = "'";
    else if (ent.equals("quot"))
      entity = "\"";
    else if (ent.equals("nbsp"))
      entity = " ";
    else
      entity = consumer.decodeEntity(ent);
    if (entity == null) throwSyntaxError("Unknown entity: &" + ent + ";");
  }

  protected static boolean endComment(StringBuilder b)
  {
    int s = b.length();
    return s > 4 && b.charAt(s - 1) == '>' && b.charAt(s - 2) == '-'
           && b.charAt(s - 3) == '-';
  }

  protected static boolean isComment(StringBuilder b)
  {
    return endComment(b) && b.charAt(0) == '-' && b.charAt(1) == '-';
  }

  protected static boolean endCDATA(StringBuilder b)
  {
    int s = b.length();
    return s > 4 && b.charAt(s - 1) == '>' && b.charAt(s - 2) == ']'
           && b.charAt(s - 3) == ']';
  }

  protected static boolean isCDATA(StringBuilder b)
  {
    return endCDATA(b) && (b.length() > 7)
           && b.substring(0, 7).equals("[CDATA[");
  }

  /** A test rig that prints lexical events, one per line. */
  public static void main(String[] args)
  {
    XMLHandler sax = new XMLHandler()
    {
      void pr(CharSequence s, CharSequence t)
      {
        System.out.println(s + " " + t);
      }

      public void wordCharacters(CharSequence chars)
      {
        pr("WD", "'" + chars + "'");
      };

      public void startElement(String kind, Map<String, String> atts)
      {
        pr("SE", kind + atts);
      };

      public void endElement(String kind)
      {
        pr("EE", kind);
      };

      public void commentCharacters(CharSequence data)
      {
        pr("CO", data);
      };

      public void startDocument()
      {
        pr("ST", "");
      };

      public void endDocument()
      {
        pr("EN", "");
      };

      public String decodeEntity(String s)
      {
        return "&" + s + ";";
      };
    };
    new XMLScanner(sax).read(new InputStreamReader(System.in));
  }

  /** An implementation of Map that shows attributes in properly-quoted XML form. */
  @SuppressWarnings("serial")
  public static class Attributes extends LinkedHashMap<String, String>
  {
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      for (String key : keySet())
      {
        b.append(" ");
        b.append(key);
        b.append("='");
        b.append(unQuote(get(key)));
        b.append("'");
      }
      ;
      return b.toString();
    }
  }

  /** Re-quote special characters within a string. */
  public static String unQuote(String s)
  {
    int len = s.length();
    StringBuilder quoted = null;
    for (int i = 0; i < len; i++)
    {
      char c = s.charAt(i);
      if (c == ' ' || c == '&' || c == '>' || c == '<' || c == '"' || c == '\''
          || (int) c >= 128)
      {
        quoted = new StringBuilder();
        break;
      }
    }
    if (quoted == null)
      return s;
    else
    {
      for (int i = 0; i < len; i++)
      {
        char c = s.charAt(i);
        quoted
              .append((c == '&' ? "&amp;"
                               : c == '>' ? "&gr;"
                                         : c == '<' ? "&ls;"
                                                   : c == '"' ? "&quot;"
                                                             : c == '\'' ? "&apos;"
                                                                        : c == ' ' ? "&nbsp;"
                                                                                  : (int) c > 128 ? ("&#"
                                                                                                     + (int) c + ";")
                                                                                                 : ("" + c)));
      }
      return quoted.toString();
    }
  }
}
