package femtoXML.app;

import femtoXML.FormatWriter;

/**
 * Represents source space outside markup.
 * @author sufrin ($Revision$)
 *
 */

public class AppSpaces implements AppTree
{
  protected String text;

  public AppSpaces(String text)
  {
    this.text = text;
  }

  /** Generates the human-readable form of the word text. */
  public String toString()
  {
    return text;
  }

  /**
   * Outputs the text of this space
   * 
   */
  public void printTo(FormatWriter out, int indent)
  {
    out.print(text);
  }

  public boolean isWord()
  {
    return false;
  }
}
