package femtoXML;
import java.util.Map;
public interface XMLHandler
{ public void   startElement(String kind, Map<String,String> atts);   // <kind key1="val1" ...>
  public void   endElement(String kind);                              // </kind>
  public void   commentCharacters(CharSequence text);                 // <!-- ... -->
  public void   wordCharacters(CharSequence text);                    // ...
  public void   startDocument();        
  public void   endDocument();          
  public String decodeEntity(String entity);                          // &entityname;         
}

