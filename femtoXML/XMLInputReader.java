package femtoXML;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * A reader that checks its raw stream for a byte-order mark, and
 * also checks for an &lt;?xml ... encoding="encoding" ... declaration
 * at the start of its base stream, then sets its encoding accordingly.
 * @author sufrin
 *
 */
final public class XMLInputReader extends Reader
{ final InputStreamReader   rawstream;
  final BufferedInputStream pushback;
  String                    encoding  =  "UTF-8";
  final static int        bufsize   = 1024;
  final static int        lookahead = 128;
  String   prop = System.getProperty("femtoXML.XMLInputReader.level");
  String   level = prop==null?"info":prop;
  boolean debug = "fine".equalsIgnoreCase(level);
  boolean info  = debug || "info".equalsIgnoreCase(level);  
  boolean none  = "none".equalsIgnoreCase(level);
  
  public String getEncoding()
  { return encoding; }

  public XMLInputReader(InputStream rawstream, String forceEncoding) throws UnsupportedEncodingException, IOException
  { pushback        = new BufferedInputStream(rawstream, bufsize);
    encoding = forceEncoding;
    if (encoding==null) setEncoding();
    this.rawstream = new InputStreamReader(pushback, encoding);
  }
  
  /** stock byte-order marks */
  final static byte[][] boms =
  { {(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF}//ucs-4be
  , {(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00}//ucs-4le
  , {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFE}//ucs-4 (2143)
  , {(byte)0xFE, (byte)0xFF, (byte)0x00, (byte)0x00}//ucs-4 (3412)
  , {(byte)0xFE, (byte)0xFF}                         //utf-16be
  , {(byte)0xFF, (byte)0xFE}                         //utf-16le
  , {(byte)0xEF, (byte)0xBB, (byte)0xBF}             //utf-8
  };
  
  /** stock bytes representing the sequence  < ? x m in different encodings */
  final static byte[][] decls =
  { {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3c}//ucs-4be
  , {(byte)0x3c, (byte)0x00, (byte)0x00, (byte)0x00}//ucs-4le
  , {(byte)0x00, (byte)0x00, (byte)0x3c, (byte)0x00}//ucs-4 (2143)
  , {(byte)0x00, (byte)0x3c, (byte)0x00, (byte)0x00}//ucs-4 (3412)
  , {(byte)0x00, (byte)0x3c, (byte)0x00, (byte)0x3f}//utf-16BE
  , {(byte)0x3c, (byte)0x00, (byte)0x3f, (byte)0x00}//utf-16LE
  , {(byte)0x3c, (byte)0x3f, (byte)0x78, (byte)0x6d}//utf-8, iso646, ascii, iso8859-?, shiftjis, euc, ... 
  , {(byte)0x4c, (byte)0x6f, (byte)0xa7, (byte)0x94}//ebcdic 
  };
  
  /** names of encodings in the same order as boms/decls */
  final static String[] kinds =
  { "UCS-4BE"
  , "UCS-4LE"
  , "UCS-4(2143)"
  , "UCS-4(3412)"
  , "UTF-16BE"
  , "UTF-16LE"
  , "UTF-8" 
  , "EBCDIC"
  };
  
  /** needmore[i] is true when kinds[i] names an encoding
      that needs to decode a declaration */

  final static boolean[] needmore =
  { true  //"UCS-4BE"
  , true  //"UCS-4LE"
  , true  //"UCS-4(2143)"
  , true  //"UCS-4(3412)"
  , false //"UTF-16BE"
  , false //"UTF-16LE"
  , false //"UTF-8"    
  };
  
  /** Find the index of a prefix of <code>bytes</code>
      among the list of byte sequences.
  */
  protected int firstIndex(byte[] bytes, byte[][] bytess)
  {
    outer:
    for (int index=0; index<bytess.length; index++)
    {
      byte[] subject = bytess[index];
      for (int i=0; i<subject.length; i++) 
          if (subject[i]!=bytes[i]) continue outer;
      return index;
    }
    return -1;
  }
  
  /** Is there an encoding with the given name? */
  protected boolean hasEncoding(String charsetName)
  { try
    { Charset.forName(charsetName);
      return true;
    }
    catch (Exception ex)
    { 
      return false;
    }
  }
  
  /** List of EBCDIC-based encodings */
  static final String[] EBCDICS =
  { "IBM500",  "IBM037",  "IBM819",
    "IBM850",  "IBM875",  "IBM924",
    "IBM1140", "IBM1141", "IBM1142",
    "IBM1143", "IBM1144", "IBM1145",
    "IBM1146", "IBM1147", "IBM1148",
    "IBM1149", "IBM1026", "IBM1047"
  };
  
  /** Find an installed EBCDIC-based encoding */
  protected String EBCDIC()
  { for (String name : EBCDICS)
        if (hasEncoding(name)) return name;
    return null;
  }
  
  /** Inspect an XML data stream and try to establish the encoding it uses */
  protected void setEncoding() throws IOException
  { pushback.mark(lookahead);
    byte[] bytes = new byte[lookahead];
    int count = pushback.read(bytes, 0, lookahead);
    if (count<4)
    {
      if (!none) System.err.printf("Warning: file to short to deduce encoding: UTF-8 assumed%n");
      return;
    }
    pushback.reset();
    int bom = firstIndex(bytes, boms);
    if (bom>=0)
    { boolean needsmore = needmore[bom]; 
      if (debug) System.err.printf("[BOM suggests %s%s]%n", kinds[bom], needsmore?" (inspecting <?xml ... ?> declaration)":"");  
      pushback.skip(boms[bom].length);   // The inputstreamreader does this anyway ...
      if (!needsmore)
      {
        encoding = kinds[bom];
        return;
      }
    }
    else
    { bom = firstIndex(bytes, decls);
      if (bom>=0)  
      {  encoding = kinds[bom];
         if (debug) System.err.printf("[<?xml ... ?> suggests %s-compatible (%02x%02x%02x%02x) (inspecting its body)]%n", kinds[bom],  bytes[0],bytes[1],bytes[2],bytes[3]);
      }
      else
      {  encoding = "UTF-8";
         if (!none) System.err.printf("[Warning: no <?xml declaration or byte-order-mark: UTF-8 assumed]%n");
         return;
      }
    }
    // Choose an arbitrary EBCDIC-compatible set and see what we can do with that
    if (encoding.equals("EBCDIC"))
    {
       encoding = EBCDIC();
       if (encoding==null)
          throw new RuntimeException("XMLInputReader cannot deduce the exact encoding of an EBCDIC file. Please set input encoding explicitly");
    }
    // Now we need to look at the declaration
    InputStreamReader peek = new InputStreamReader(new ByteArrayInputStream(bytes), encoding);  
    StringBuilder b = new StringBuilder();
    int ch;
    while ((ch=peek.read())>0 && ch!='>') b.append((char)ch);
    if (ch<=0) throw new RuntimeException("XMLInputReader expecting a valid <?xml ... encoding=...?>  declaration, read:"+b.toString());
    Pattern enc = Pattern.compile("encoding\\s*=\\s*('[^']*'|\"[^\"]*\")");
    Matcher mat = enc.matcher(b);
    if (mat.find())
    {
      encoding = mat.group(1);
      encoding = encoding.substring(1, encoding.length()-1);
      if (info) System.err.printf("[Declared XML encoding is %s]%n", encoding);
    }
    else
    {
      if (!none) System.err.printf("[Warning: no <?xml encoding=...?> declaration; using input encoding %s]%n", encoding);
    }
    
  }
  
  @Override
  public int read(char[] cbuf, int off, int len) throws IOException
  {
    return rawstream.read(cbuf, off, len);
  }

  @Override
  public void close() throws IOException
  {
    rawstream.close();
  }
}

