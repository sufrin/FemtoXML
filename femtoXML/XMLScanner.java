package femtoXML;

import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Stack;
/**
 * An <code>XMLScanner</code> is primed with an <code>XMLHandler</code>,
 * and then can be used(and re-used) to read XML from a
 * <code>java.io.LineNumberReader</code>. There are almost no limitations on
 * the form of the XML it will read, <b>but</b>:
 * <ul>
 * <li>No attempt is made to recover from XML parsing errors.</li>
 * <li>DOCTYPE declarations are ignored</li>
 * <li>Namespace prefixes are incorporated into entity names so namespace
 * processing has to be performed further up the analysis chain</li>
 * </ul>
 * 
 * <p>
 */
public class XMLScanner implements XMLHandler.XMLLocator
{
  protected XMLHandler           consumer;
  protected LineNumberReader     reader;
  protected boolean             expandEntities = true;
  protected int                  elementLevel;
  private final char    
                QUOT = 0x22, 
                APOS = 0x27;
  
  /** The line where the current symbol started */
  protected int lineNumber; 

  protected void throwSyntaxError(String error) 
  { throw new XMLSyntaxError(this, error); }

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
  
  /**
   * <code>expandEntities</code> is true if entities are to be expanded: this
   * method sets its state.
   */
  public void setExpandEntities(boolean expanding)
  { this.expandEntities = expanding; }
  
  /**
   * Returns the current <code>expandEntities state</code>: true if entities
   * are being expanded.
   */
  public boolean getExpandEntities() 
  { return expandEntities; }

  /** Return the current source line number */
  public int lineNumber()
  {
    return lineNumber;
  }
  
  /** Return the current source description */
  public String getDescription()
  {  if (entities.isEmpty())
        return description;
     else
     {
       StringBuilder b = new StringBuilder();
       b.append(description);
       for (int i=0; i<entitynames.size(); i++)
           b.append(String.format(" within &%s;", entitynames.get(i)));
       return b.toString();
     }
  }
  
  /** Human-readable string describing the source of the current input stream. */
  protected String description;
  
  /**
   * Set a string describing the source of the current input stream. Used for
   * human-readable diagnostics.
   */
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
       throwSyntaxError(token + " expected; found " + this.token + " (" + this.value + ")");
  }
  
  /** Lexeme representation  */
  private enum Lex
  {
    ENDSTREAM("END-OF-XML-STREAM"), 
    OPENTAG("<"), 
    CLOSETAG(">"), 
    OPENENDTAG( "</"), 
    FINISHELEMENT("/>"), 
    CONTENT("CONTENT"), 
    ENTITY("ENTITY"),
    NAME("NAME"), 
    CDATA("<![CDATA[ ..."), 
    QUOTE("' or \""), 
    EQUALS("="), 
    COMMENT("<!-- ..."), 
    PI("<? ... ?>"), 
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
   * Read XML from the given LineNumberReader, invoking the current
   * <code>XMLHandler consumer</code>'s methods at appropriate times and
   * keeping track of line numbers. If a client needs to keep track of column
   * numbers as well then <code>nextRawChar()</code> should be overridden in a
   * subclass.
   * 
   * @param reader --
   *          the reader
   * @param description --
   *          a human-readable description
   */
  public void read(LineNumberReader reader, String description)
  { if (description != null) 
       setDescription(description); 
    else
    if (this.description==null) 
       setDescription("<anonymous input stream>");
    this.reader = reader;
    reader.setLineNumber(1);
    entities     = new Stack<Reader>();
    entitynames  = new Stack<String>();
    entityLevels = new Stack<Integer>();
    entityLevels.push(0);
    ch = 0;
    elementLevel = 0;
    consumer.setLocator(this);
    consumer.startDocument();
    nextToken();
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
        case COMMENT: // <!-- ... -->
          if (consumer.wantComment())
              consumer.commentCharacters(value);
          break;
        case DOCTYPE: // <!DOCTYPE ...
          if (consumer.wantDOCTYPE())
             consumer.DOCTYPECharacters(value);
          break;
        case PI: // <? ...
          if (consumer.wantPI())
             consumer.PICharacters(value);
          break;
        case NAME:
        case CONTENT:
          consumer.contentCharacters(value, false);
          break;
        case CDATA:
          consumer.contentCharacters(value, true);
          break;
        case OPENENDTAG: // </ tag >
          checkToken(Lex.NAME);
          String tagname = value;
          consumer.endElement(value);
          checkToken(Lex.CLOSETAG);
          elementLevel--;
          if (elementLevel<0)
             throwSyntaxError(String.format("Superfluous closing tag: </%s>", value));
          if (elementLevel<entityLevels.peek())
             throwSyntaxError(String.format("Superfluous closing tag: </%s> in entity &%s;", tagname, entitynames.peek()));
          break;
        case OPENTAG: // <id id="..." ...
        {
          XMLAttributes atts = consumer.newAttributes(expandEntities);
          inElement = true;
          nextToken();
          if (token != Lex.NAME)
            throwSyntaxError(String.format("Element name expected; found %s %s", token, value));
          String tag = value;
          nextToken();
          while (token == Lex.NAME)
          {
            String key = value;
            skipToken(Lex.EQUALS);
            if (token == Lex.QUOTE)
            {
              atts.put(key.intern(), value.intern());
              nextToken();
            }
            else
              throwSyntaxError("String expected after " + key + "= : found " + token + " ("+value+")");
          }
          consumer.startElement(tag, atts);
          if (token == Lex.FINISHELEMENT) // />
            consumer.endElement(tag);
          else if (token != Lex.CLOSETAG)
            throwSyntaxError("Attribute name or > or /> expected in start tag: found " + token + " (" + value + ")");
          inElement = false;
          elementLevel++;
        }
        break;
        default:
          throwSyntaxError("Unexpected token: " + token + " " + value);
      }
      nextToken();
    }
  }

  /** The current symbol's characters, if it's a class */
  protected String  value     = null;

  /** The current character */
  protected int     ch;

  /** Expansion of the last character entity read */
  protected String  entity;
  
  /** Name of the last non-character entity read */
  protected String entityName;

  /** True iff currently reading an element header < ... /> or < ... > */
  protected boolean inElement = false;

  /** Read the next token */
  protected void nextToken()
  {
    if (0 <= ch && ch <= ' ')
    {
      if (!inElement && consumer.wantSpaces())
      {
        StringBuilder b = new StringBuilder();
        do
        { b.append((char)ch);
          nextRawChar();
        }
        while (0 <= ch && ch <= ' ');
        consumer.spaceCharacters(b);
      }
      else
      {
        do
        {
          nextRawChar();
        }
        while (0 <= ch && ch <= ' ');
      }
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
    else if (inElement && (ch == APOS || ch == QUOT)) 
    {
      int closeQuote         = ch;
      int entitynestinglevel = entities.size();
      StringBuilder b = new StringBuilder();
      nextChar();
      // closing quote must be at the same entity-nesting level as the opening
      while (0 <= ch && !(ch == closeQuote && entities.size()==entitynestinglevel))
      {
        if (expandEntities && ch == '&')
          if (isCharEntity)
            b.append(theCharEntity);
          else          
            pushEntity(entityName);          
        else
          b.append((char) ch);
        nextChar();
      }
      token = Lex.QUOTE;
      value = b.toString();
      nextRawChar();
    }
    else if (inElement && ch == '/')
    {
      nextRawChar();
      if (ch == '>')
      {
        nextRawChar();
        token = Lex.FINISHELEMENT;
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
        token = Lex.OPENENDTAG;
      }
      else if (ch == '?')
      { boolean want = consumer.wantPI();
        nextRawChar();
        int lastch = ch;
        StringBuilder b = new StringBuilder();
        while (0 <= ch && !(ch == '>' && lastch == '?'))
        {
          if (want) b.append((char) ch);
          lastch = ch;
          nextRawChar();
        }
        if (ch == -1)
          throwSyntaxError("<? with runaway body ...");
        else
        {
          nextRawChar();
          value = want ? b.substring(0, b.length() - 1) : "... skipped PI ...";
          token = Lex.PI;
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
        { boolean want = consumer.wantDOCTYPE();
          doctypeMatcher.reset();
          StringBuilder b = want ? new StringBuilder() : null;
          int count = 1;
          while (0 <= ch && count > 0)
          { doctypeMatcher.append((char) ch);
            if (want) b.append((char) ch);
            nextRawChar();
            if (ch == '<')
              count++;
            else 
            if (ch == '>') 
              count--;
          }
          if (count != 0) 
             throwSyntaxError("<!DOCTYPE with runaway body ...");
          if (!doctypeMatcher.match())
             throwSyntaxError("<!DOCTYPE ... > expected.");
          token = Lex.DOCTYPE;
          value = want ? b.substring(8, b.length()) : "... skipped doctype ...";
          nextRawChar();
        }
        else
        // Assume <!-- comment -->
        { boolean want = consumer.wantComment();
          commentMatcher.reset();
          StringBuilder b = want ? new StringBuilder() : null;
          do
          {
            while (0 <= ch && ch != '>')
            { commentMatcher.append((char)ch);
              if (want) b.append((char) ch);
              nextRawChar();
            }
            if (ch > 0)
            { commentMatcher.append((char)ch);
              if (want) b.append((char) ch);
              nextRawChar();
            }
          }
          while (0 <= ch && !commentMatcher.match());
          if (commentMatcher.match())
          {
            value = want ? b.substring(2, b.length() - 3) : "... skipped comment ...";
            token = Lex.COMMENT;
          }
          else
            throwSyntaxError("<!-- ... --> expected");
        }
      }
      else
        token = Lex.OPENTAG;
    }
    else if (ch == '>')
    {
      nextRawChar();
      token = Lex.CLOSETAG;
    }
    else
    // a piece of text appears that could be content -- but if it looks like an identifier then
    // we say it's that, and treat it as content only when out of element tag context.
    {
      // leading & is a special case because it was read by nextRawChar
      if (inElement && ch=='&') 
      {   nextEntity();
          throwSyntaxError(String.format ("Entity reference &%s; out of place in element tag", entityName));
      }
      if (expandEntities && ch == '&')
      {
        nextEntity();
        if (!isCharEntity) 
        { pushEntity(entityName); 
          nextRawChar(); 
          nextToken();
        }
        else 
          nextContent();
      }
      else
        nextContent();
    }
  }
  
  protected void nextContent()
  {
    StringBuilder b = new StringBuilder();
    token = Lex.NAME;
    while (ch > ' ' && ch != '<' && ch != '>'
           && !(inElement && (ch == '/' || ch == '=')))
    {
      if (expandEntities && ch == '&')
        if (isCharEntity)
          b.append(theCharEntity);
        else
          pushEntity(entityName);
      else
        b.append((char) ch);
      // It's an identifier as long as it consists only of identifier
      // characters
      if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != ':') token = Lex.CONTENT;
      nextChar();
    }
    value = b.toString();    
  }
  
  /** The stack of open entity-bodies */
  protected Stack<Reader> entities;
  
  /** The stack of open entity names */
  protected Stack<String> entitynames;
  
  /** The stack of element-nesting levels corresponding to open entities */
  protected Stack<Integer> entityLevels;
    
  protected void pushEntity(String entityName)
  { Reader expansion = consumer.decodeEntity(entityName);
    if (expansion == null) 
        throwSyntaxError(String.format("Cannot find expansion of &%s;", entityName));
    if (entitynames.contains(entityName))
        throwSyntaxError(String.format("Recursion in expansion of &%s;", entityName));
    entities.push(expansion);
    entitynames.push(entityName);
    entityLevels.push(elementLevel);
  }

  /**
   * Read the next character; gobble the next entity if
   * <code>expandingEntities</code>
   */
  protected void nextChar()
  {
    nextRawChar();
    if (expandEntities && ch == '&')
    {
      nextEntity();
    }
  }
  
  /** We tack a space on the end of each entity 
   *  expansion to ensure that the well-formedness 
   *  machinery works; this variable is true
   *  when we are prepared to substitute a
   *  single space at the end of stream.
   */
  protected boolean tackSpace;

  /** Read the next raw character. */
  protected void nextRawChar()
  {
    try
    {    while (!entities.isEmpty())
         { ch = entities.peek().read();
           if (ch>=0)
           {  tackSpace = true;
              return; 
           }
           else
           if (tackSpace) 
           { ch = ' ';
             tackSpace = false;
             return;
           }
           entities.pop().close();
           String name  = entitynames.pop();
           int    level = entityLevels.pop();
           if (elementLevel!=level)
              throwSyntaxError(String.format("Entity &%s; is not well-formed.", name));
         }
         ch = reader.read();
         if (ch<0) reader.close();
    }
    catch (XMLSyntaxError ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  boolean isCharEntity;
  char    theCharEntity;
  
  /**
   * Read the next entity: <code>entityName</code> is set to the name. If the
   * entity is a character entity, then <code>theCharEntity</code> is set to
   * it, and <code>isCharEntity</code> is set true. The variable
   * <code>ch</code> is always set to <code>'&'</code>.
   */
  protected void nextEntity()
  {
    entityName = "";
    nextRawChar();
    while (Character.isLetterOrDigit(ch) || ch == '_' || ch == ':')
    {
      entityName = entityName + ((char) ch);
      nextRawChar();
    }
    if (ch!=';') throwSyntaxError(String.format("Runaway entity name &%s (should end in ';')", entityName));
    theCharEntity = consumer.decodeCharEntity(entityName);
    isCharEntity = theCharEntity > 0;
    ch = '&';
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
  
  /**
   * A class that stores a fixed-length prefix and suffix of a sequence of
   * characters that is being recognised, rather than storing the whole
   * sequence. Instances of this class are used when scanning constructs
   * (comments and doctype declarations) that are recognised by their prefix and
   * suffix but that the consumer may not want. This avoids having to store the
   * whole of such a construct in order to recognise it, only to throw it away
   * once it has been recognised.
   * 
   * @author sufrin
   * 
   */
  protected abstract static class Matcher
  {
    public Matcher(int nfront, int nback)
    {
      front = new char[nfront];
      back = new char[nback];
      length = 0;
    }

    char[] front, back;

    int    length;

    public void reset()
    {
      length = 0;
      for (int i = 0; i < front.length; i++)
        front[i] = '\000';
      for (int i = 0; i < back.length; i++)
        back[i] = '\000';
    }

    public void append(char c)
    {
      if (length < front.length) front[length++] = c;
      for (int i = 1; i < back.length; i++)
        back[i] = back[i - 1];
      if (back.length > 0) back[0] = c;
    }

    public abstract boolean match();
  }
  
  static Matcher commentMatcher = new Matcher(2, 3)
  {
    public boolean match()
    {
       return front[0]=='-' && front[1]=='-' && back[0]=='>' && back[1]=='-' && back[2]=='-';
    }
  };
  
  static Matcher doctypeMatcher = new Matcher(8, 0)
  {
    public boolean match()
    {
       return front[0]=='D' && front[1]=='O' && front[2]=='C' && front[3]=='T' &&
               front[4]=='Y' && front[5]=='P' && front[6]=='E' && front[7]==' ';
    }
  };

}


