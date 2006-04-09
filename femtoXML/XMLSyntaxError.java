package femtoXML;

/** XMLSyntaxError errors are generated by scanners and parsers.
 *  
 */
@SuppressWarnings("serial")
public class XMLSyntaxError extends RuntimeException
{
  public XMLSyntaxError(String error)
  {
    super(error);
  }
  
  public XMLSyntaxError(XMLHandler.XMLLocator locator, String error)
  { this(error, locator.getDescription(), locator.lineNumber());
  }

  public XMLSyntaxError(String error, String description, int lineNumber)
  {
    super(String.format("Line %d: %s -- %s", lineNumber, description, error));
  }

}