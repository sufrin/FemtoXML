package femtoXML;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * An extended PrintWriter that supports indented prettyprinting.
 * 
 * @author sufrin
 * 
 */
public class FormatWriter extends PrintWriter
{
  public FormatWriter(OutputStream out)
  {
    super(out, true);
    lnPending = false;
    chars = 0;
  }

  public FormatWriter(Writer out)
  {
    super(out, true);
    lnPending = false;
    chars = 0;
  }

  /** There's a newline pending. */
  protected boolean lnPending;

  /** Number of characters output on the current line */
  protected int     chars;

  /** Get the number of characters output on the current line */
  public int getChars()
  {
    return chars;
  }

  /**
   * Returns true if a string of the given size will fit within the current
   * margin
   */
  public boolean withinMargin(int size)
  {
    return chars + size < margin;
  }

  /** The desired right margin */
  protected int margin = 60;

  /** Set the right margin */
  public void setMargin(int margin)
  {
    this.margin = margin;
  }

  /** Return the right margin */
  public int getMargin()
  {
    return margin;
  }

  /** Clients must transform exotic characters into character entities */
  protected boolean charEntities = false;

  /**
   * Should clients transform exotic characters (with codes >=128) into
   * character entitities
   */
  public boolean getCharEntities()
  {
    return charEntities;
  }

  /**
   * Should clients transform exotic characters (with codes >=128) into
   * character entitities
   */
  public void setCharEntities(boolean useCharEntities)
  {
    this.charEntities = useCharEntities;
  }

  /** Flush the stream */
  public void flush()
  {
    pendingln();
    super.flush();
  }

  /** Generate a new line if there is one pending. */
  protected void pendingln()
  {
    if (lnPending)
    {
      super.println();
      chars = 0;
      lnPending = false;
    }
  }

  /** Force a newline and move to the given indentation */
  public void forceln(int indentation)
  {
    println();
    indent(indentation);
  }

  /** Output any pending newline and move to the given indentation */
  public void indent(int indentation)
  {
    if (indentation < 0)
    {
      return;
    } 
    else
    {
      pendingln();
      for (int i = 0; i < indentation; i++)
        super.print(" ");
      chars = indentation;
    }
  }

  public void print(char c)
  {
    super.print(c);
    chars++;
  }

  /** Finish the current line and leave the newline pending. */
  public void println()
  {
    lnPending = true;
  }

  public void print(String s)
  {
    super.print(s);
    chars += s.length();
  }

  /** Print the given string and finish the current line. */
  public void println(String s)
  {
    super.print(s);
    println();
  }
}
