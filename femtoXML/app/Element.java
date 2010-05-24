package femtoXML.app;

import java.util.*;
import femtoXML.FormatWriter;
import femtoXML.XMLAttributes;
import femtoXML.XMLComposite;

/**
 * Represents an XML element.
 * 
 * @author sufrin
 * 
 */
public class Element extends NodeImp implements Node, XMLComposite<Node>,
		Iterable<Node>
{   /** The kind of the node -- including a prefix if the node has not had its namespace resolved, or if the prefix is undeclared in the scope */
	protected String kind;
	
	/** The namespace URI if the kind was prefixed and the namespace has been resolved, else null */
	protected String nameSpace = null;

	/** Mapping from attribute names to attributes */
	protected XMLAttributes attrs;
	
	

	/** Subtrees of this node */
	protected Vector<Node> subtrees = new Vector<Node>();

	/** True if this node is interested in spaces. */
	protected boolean wantSpaces;

	/** True if this node is interested in spaces. */
	public boolean wantSpaces()
	{
		return wantSpaces;
	}

	/** Complete the formation of the element.  */
	public Node close()
	{  
		return this;
	}

	public Element(String kind, XMLAttributes attrs)
	{
		this.kind = kind;
		this.attrs = attrs;
		String xmlSpace = attrs.get("xml:space");
		wantSpaces = xmlSpace != null && xmlSpace.equalsIgnoreCase("preserve");
	}
	
	/** Resolve the namespace of this element by looking up prefix and changing kind accordingly. 
	 *  <br/>
	 *  PROVISO: the element's  attributes must have had their enclosing scope set. 
	 *  This is ensured by <code>FemtoXML.XMLParser.startElement</code>, so an element can have its name
	 *  space resolved just after construction (for example within the tree factory
	 *  method that is invoked by a parser).
	 *  It is also true for the attributes of elements that have themselves had their parents set.
	 *  <p>
	 *  If the proviso isn't met, then the method has no effect.
	 *  @TODO resolve attribute-name namespaces.
	 */
	public void resolveNameSpace()
	{   if (nameSpace!=null) return;
		int i = kind.indexOf(':');
		if (i > 0)
		{
			String prefix = kind.substring(0, i);
			nameSpace = attrs.getNameSpace(prefix);
			if (nameSpace != null)
				kind = kind.substring(i + 1);
		}
		else
		{
			nameSpace = attrs.getNameSpace("xmlns");
		}
	}
	
	public void add(Node t)
	{
		if (t != null)
		{
			subtrees.add(t);
			t.setParent(this);
		}
	}
	
	public void setParent(Element parent)
	{
	   super.setParent(parent);
	   if (attrs!=null) attrs.setEnclosingScope(parent.attrs);
	}
	
	public String getKind()
	{
		return kind;
	}

	public Stream<Node> iterator()
	{
		return Stream.appIterator(subtrees);
	}

	public String toString()
	{
		return String.format("<%s%s>%s</%s>", kind, attrs,
				formatIter(subtrees), kind);
	}

	public static String formatIter(Vector<Node> subtrees)
	{
		StringBuilder s = new StringBuilder();
		boolean first = true;
		for (Node t : subtrees)
		{
			if (first)
				first = false;
			else
				s.append(" ");
			s.append(t.toString());
		}
		return s.toString();
	}
    
	protected void printNameSpace(FormatWriter out)
	{
		if (nameSpace!=null) out.print(String.format(" xmlns='%s'", nameSpace));
	}
	
	public void printTo(FormatWriter out, int indent)
	{
		out.indent(indent);
		if (subtrees.size() == 0) // Can we abbreviate the tree?
		{
			out.print(String.format("<%s", kind));
			attrs.printTo(out, indent + 4);
			out.print("/>");
		} else if (wantSpaces || indent < 0)
		{
			out.print(String.format("<%s", kind));
			attrs.printTo(out, -1);
			printNameSpace(out);
			out.print(">");
			for (Node t : subtrees)
				t.printTo(out, -1);
			out.println(String.format("</%s>", kind));
		} else
		{
			out.print(String.format("<%s", kind));
			attrs.printTo(out, indent + 4);
			printNameSpace(out);
			out.println(">");
			boolean wasWord = false; // Last printed tree was a Word
			for (Node t : subtrees)
			{
				boolean isWord = t.isWord();
				if (isWord && wasWord
						&& out.withinMargin(t.toString().length()))
				{
					out.print(' ');
					t.printTo(out, 0);
				} else
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

	public String elementName()
	{
		return kind;
	}

	public String getAttr(String attrName)
	{
		return attrs == null ? null : attrs.get(attrName);
	}
	
	/////////////////////////////////////////// CONVENIENCE METHODS //////////////////////////////////
	
	/** Add a node by hand */
	public Element with(Node t)
	{ add(t);
      return this;	 
	}
	
	/** Add a collection of nodes by hand */
	public Element with(Iterable<Node> c)
	{ for (Node n : c) add(n);
      return this;	 
	}
	
	/** Add a collection of collections of nodes by hand */
	public Element with(Stream<Stream<Node>> its)
	{ for (Stream<Node> it : its) 
		  for (Node n: it)
			  add(n);
      return this;	 
	}
	
	/** Utility method to construct an element by hand */
	public static Element element(String kind, String ... attrs) 
	{ femtoXML.XMLAttributes map = new femtoXML.XMLAttrMap();
	  for (int i=0; i<attrs.length; i+=2) map.put(attrs[i], attrs[i+1]); 
	  return new Element(kind, map);
	}
	
	public Node visit(Visitor v) {
		return v.visit(this);
	}

	public XMLAttributes getAttrs() {
		return attrs;
	}
	
	public Node copy() { 
		   Element r = new Element(kind, attrs); 
    	   for (Node n : iterator()) r.add(n.copy());
		   return r;
    }

}
