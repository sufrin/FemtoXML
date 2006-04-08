package femtoXML;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
/**
 * An <code>XMLScanner</code> is primed with an <code>XMLHandler</code>, and then can be used(and re-used) to read XML from a
 * <code>java.io.LineNumberReader</code>.
 * There are almost no limitations on the form of the XML it will read, <b>but</b>:
 * <ul>
 *  <li>No attempt is made to recover from XML parsing errors.</li>
 *  <li>Only character entities are expanded.</li>
 *      Should a client class require other forms of entity to be expanded, 
 *      then it is possible to build a suitable subclass of <code>LineNumberReader</code>
 *      that performs entity expansion. We prefer to treat character-macro-expansion functionality
 *      and XML scanning/parsing orthogonally.
 *  <li>DOCTYPE declarations are ignored</li>
 *  <li>Processing instructions are ignored</li>
 *  <li>Namespace prefixes are incorporated into entity names so namespace processing has to be performed further up the analysis chain</li>
 * </ul>
 * 
 * <p> 
 * */
public class XMLScanner implements XMLHandler.XMLLocator
{
  protected XMLHandler           consumer;
  protected LineNumberReader     reader;
  protected boolean             expandEntities = true;
  
  /** The line where the current symbol started */
  protected int lineNumber; 

  protected void throwSyntaxError(String error) 
  { throw new XMLSyntaxError(error, description, lineNumber()); }

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
  
  /** Expanding is true if entities are to be expanded: this method sets the current expandEntities state. */
  public void setExpandEntities(boolean expanding)
  { this.expandEntities = expanding; }
  
  /** Returns the current expandEntities state: true if entities are being expanded. */
  public boolean getExpandEntities() 
  { return expandEntities; }

  /** Return the current source line number */
  public int lineNumber()
  {
    return lineNumber;
  }
  
  /** Return the current source description */
  public String getDescription()
  {
     return description;
  }
  
  /** Human-readable string describing the source of the current input stream. */
  protected String description;
  
  /** Set a string describing the source of the current input stream. Used for human-readable diagnostics. */
  public void setDescription(String description)
  { this.description = description;
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
    ENDSTREAM("END-OF-XML-STREAM"), 
    POINTBRA("<"), 
    POINTKET(">"), 
    POINTBRASLASH( "</"), 
    SLASHPOINTKET("/>"), 
    WORD("WORD"), 
    IDENTIFIER("IDENTIFIER"), 
    CDATA("<![CDATA[ ..."), 
    SQUOTE("' or \""), 
    EQUALS("="), 
    COMMENT("<!-- ..."), 
    PROCESS("<? ... ?>"), 
    DOCTYPE("<!DOCTYPE ...");
    
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
  
  public void read(LineNumberReader reader) { read(reader, null); }
  
  /**
   * Read XML from the given LineNumberReader, invoking the current <code>XMLHandler consumer</code>'s methods at
   * appropriate times and keeping track of line numbers. If a client needs to keep track of column
   * numbers as well then <code>nextRawChar()</code> should be overridden in a subclass.
   * @param reader -- the reader
   * @param description -- a human-readable description
   */
  public void read(LineNumberReader reader, String description)
  { if (description != null) 
       setDescription(description); 
    else
    if (this.description==null) 
       setDescription("<anonymous input stream>");
    this.reader = reader;
    entities = new Stack<Reader>();
    ch = 0;
    nextToken();
    consumer.setLocator(this);
    consumer.startDocument();
    readBody();
    consumer.endDocument();
    try
    {
      reader.close();
    }
    catch (Exception e)
    {}
  }
  
  protected void readBody()
  {
    while (token != Lex.ENDSTREAM)
    {
      switch (token)
      {
        case DOCTYPE:
          break;
        case PROCESS:
          consumer.PICharacters(value);
          break;
        case IDENTIFIER:
        case WORD:
          consumer.wordCharacters(value, false);
          break;
        case CDATA:
          consumer.wordCharacters(value, true);
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
          Map<String, String> atts = new Attributes(expandEntities);
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
              throwSyntaxError("Found " + token + " when string expected in "
                               + key + "=...");
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
    lineNumber = reader.getLineNumber();
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
        if (expandEntities && ch == '&')
          if (charEntity) b.append(entity); else { pushEntity(entity); }
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
            throwSyntaxError("<!-- ... --> expected");
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
      if (expandEntities && ch == '&')
      {
          token = Lex.WORD;
          nextEnt();
          if (charEntity)
          { 
            b.append(entity);
          } 
          else 
          { pushEntity(entity); }//TODO
          nextChar();
      }
      while (ch > ' ' && ch != '<' && ch != '>'
             && !(inElement && (ch == '/' || ch == '=')))
      {
        if (expandEntities && ch=='&')
          if (charEntity) b.append(entity); else { pushEntity(entity); } //TODO
        else
          b.append((char) ch);
        if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != ':') token = Lex.WORD;
        nextChar();
      }
      value = b.toString();
    }
  }
  
  /** The stack of open entity-bodies */
  protected Stack<Reader> entities;
  
  protected void pushEntity(String body)
  {
    entities.push(new StringReader(body));
  }

  /** Read the next character -- expandEntities character entities */
  protected void nextChar()
  {
    nextRawChar();
    if (expandEntities && ch == '&')
    {
      nextEnt();
      ch = '&';
    }
  }

  /** Read the next raw character. */
  protected void nextRawChar()
  {
    try
    {    while (!entities.isEmpty())
         { ch = entities.peek().read();
           if (ch>=0) return;
           entities.pop();
         }
         ch = reader.read();

    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  boolean charEntity;
  
  /**
   * Read and expand the next entity; the variable 'entity' is set to the
   * expansion.
   */
  protected void nextEnt()
  {
    String ent = "";
    charEntity = true;
    nextRawChar();
    while (' ' < ch && ch != ';')
    {
      ent = ent + ((char) ch);
      nextRawChar();
    }
    if (ent.equals("amp"))
      entity = "&";
    else if (ent.equals("lt"))
      entity = "<";
    else if (ent.equals("gt"))
      entity = ">";
    else if (ent.equals("apos"))
      entity = "'";
    else if (ent.equals("quot"))
      entity = "\"";
    else if (ent.equals("nbsp"))
      entity = " ";
    else if (ent.matches("#[0-9]+"))
      entity = ""+(char)Integer.parseInt(ent.substring(1));
    else if (ent.matches("#[Xx][0-9a-fA-F]+"))
      entity = ""+(char)Integer.parseInt(ent.substring(2), 16);
    else
    { charEntity = false;
      entity = consumer.decodeEntity(ent);
      if (entity == null) throwSyntaxError("Unknown entity: &" + ent + ";");
    }
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

      public void wordCharacters(CharSequence chars, boolean cdata)
      {
        pr("WD", "'" + chars + "'");
      };
      
      public void PICharacters(CharSequence chars)
      {
        pr("PI", "'" + chars + "'");
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
      }

      public void setLocator(XMLLocator loc)
      {
        // TODO Auto-generated method stub
        
      };
    };
    new XMLScanner(sax).read(new LineNumberReader(new InputStreamReader(System.in)));
  }

  /** An implementation of Map that shows attributes in properly-quoted XML form if
   *  expandEntities is true.
   */
  @SuppressWarnings("serial")
  public static class Attributes extends LinkedHashMap<String, String>
  { protected boolean expanding = true;
    public Attributes(boolean expanding) { this.expanding = expanding; }
    public String toString()
    {
      StringBuilder b = new StringBuilder();
      for (String key : keySet())
      {
        b.append(" ");
        b.append(key);
        b.append("='");
        if (expanding)
          b.append(unQuote(get(key), false));
        else
          b.append(get(key));
        b.append("'");
      }
      ;
      return b.toString();
    }
  }

  /** Re-quote special characters within a string. */
  public static String unQuote(String s, boolean quotespace)
  {
    int len = s.length();
    StringBuilder quoted = null;
    for (int i = 0; i < len; i++)
    {
      char c = s.charAt(i);
      if ((c == ' ' && quotespace) || c == '&' || c == '>' || c == '<' || c == '"' || c == '\''
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
              .append(( c == '&' ? "&amp;"
                      : c == '>' ? "&gt;"
                      : c == '<' ? "&lt;"
                      : c == '"' ? "&quot;"
                      : c == '\'' ? "&apos;"
                      : (c == ' ' && quotespace) ? "&nbsp;"
                      : (int) c > 128 ? ("&#" + (int) c + ";")
                      : ("" + c)));
      }
      return quoted.toString();
    }
  }
}
