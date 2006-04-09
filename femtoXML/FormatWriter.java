package femtoXML;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class FormatWriter extends PrintWriter
{
  public FormatWriter(OutputStream out) { super(out, true); lnPending = false; chars = 0; }
  public FormatWriter(Writer out)       { super(out, true); lnPending = false; chars = 0; }

  /** There's a newline pending. */
  protected boolean lnPending;
  
  protected int chars;
  
  /** Get the number of characters output on the current line */
  public int getChars() { return chars; }
  
  /** Returns true if a string of the given size will fit within the current margin */
  public boolean withinMargin(int size)
  {
    return chars + size < margin;
  }
  
  int margin = 80;
  
  public void setMargin(int margin) { this.margin = margin; }
  
  public int getMargin() { return margin; }
  
  /** Expand characters into character entities */
  protected boolean charEntities = false;
  
  /** Expanding characters into character entitities */
  public boolean getCharEntities() { return charEntities; }
  
  /** Set whether characters are expanded as character entitities */
  public void setCharEntities(boolean useCharEntities) { this.charEntities = useCharEntities; }
  
  /** Flush the stream */
  public void flush()
  { pendingln();
    super.flush();
  }

  /** generate a new line if there is one pending. */
  public void pendingln()
  { if (lnPending)
    { super.println();
      chars = 0;
      lnPending = false;
    }
  }

  public void forceln(int indentation)
  {
    println();
    indent(indentation);
  }
  
  public void indent(int indentation)
  {  pendingln();
     for (int i=0; i<indentation; i++) super.print(" ");
     chars = indentation;
  }
  
  public void print(char c)
  {
     super.print(c);
     chars++;
  }

  /** Mark the current line ended. */
  public void println()
  {
     lnPending = true;
  }

  /** Equivalent to <code>print(s); println()</code>. */
  public void println(String s)
  {
     super.print(s);
     println();
  }
}
