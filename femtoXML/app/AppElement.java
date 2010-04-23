package femtoXML.app;

import java.util.*;
import femtoXML.FormatWriter;
import femtoXML.XMLAttributes;
import femtoXML.XMLComposite;

/**
 * Represents an XML element.
 * @author sufrin
 *
 */
public class AppElement extends AppTreeImp
       implements AppTree, XMLComposite<AppTree>, Iterable<AppTree>
{
  protected String          kind;

  protected XMLAttributes   attrs;

  protected Vector<AppTree> subtrees = new Vector<AppTree>();
  
  /** True if this node is interested in spaces. */
  protected boolean         wantSpaces;

  /** True if this node is interested in spaces. */
  public boolean wantSpaces()
  {
    return wantSpaces;
  }

  public AppTree close()
  {
    return this;
  }

  public AppElement(String kind, XMLAttributes attrs)
  {
    this.kind = kind;
    this.attrs = attrs;
    String xmlSpace = attrs.get("xml:space");
    wantSpaces = xmlSpace != null && xmlSpace.equalsIgnoreCase("preserve");
  }

  public void add(AppTree t)
  { if (t!=null)
    { subtrees.add(t);
      t.setParent(this); 
    }
  }

  public String getKind()
  {
    return kind;
  }

  public AppIterator<AppTree> iterator()
  {
    return AppIterator.appIterator(subtrees);
  }


  public String toString()
  {
    return String.format("<%s%s>%s</%s>",
                         kind,
                         attrs,
                         formatIter(subtrees),
                         kind);
  }

  public static String formatIter(Vector<AppTree> subtrees)
  {
    StringBuilder s = new StringBuilder();
    boolean first = true;
    for (AppTree t : subtrees)
    {
      if (first)
        first = false;
      else
        s.append(" ");
      s.append(t.toString());
    }
    return s.toString();
  }

  public void printTo(FormatWriter out, int indent)
  {
    out.indent(indent);
    if (subtrees.size() == 0) // Can we abbreviate the tree?
    {
      out.print(String.format("<%s", kind));
      attrs.printTo(out, indent + 4);
      out.print("/>");
    }
    else if (wantSpaces || indent < 0)
    {
      out.print(String.format("<%s", kind));
      attrs.printTo(out, -1);
      out.print(">");
      for (AppTree t : subtrees)
        t.printTo(out, -1);
      out.println(String.format("</%s>", kind));
    }
    else
    {
      out.print(String.format("<%s", kind));
      attrs.printTo(out, indent + 4);
      out.println(">");
      boolean wasWord = false; // Last printed tree was a Word
      for (AppTree t : subtrees)
      {
        boolean isWord = t.isWord();
        if (isWord && wasWord && out.withinMargin(t.toString().length()))
        {
          out.print(' ');
          t.printTo(out, 0);
        }
        else
        {
          t.printTo(out, indent + 2);
          out.println();
        }
        wasWord = isWord;
      }
      out.println();
      out.indent(indent);
      out.println(String.format("</%s>", kind));
    }
  }

  public boolean isWord()
  {
    return false;
  }
  
  public boolean isElement()
  {
    return true;
  }
}

