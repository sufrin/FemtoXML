package femtoXML.app;

import java.util.HashMap;
import java.util.Map;

import femtoXML.XMLAttrMap;
import femtoXML.XMLAttributes;
import femtoXML.XMLTreeFactory;
import femtoXML.XMLHandler.XMLLocator;

/**
 * A <code>XMLTreeFactory</code> that builds
 * <code>Element, Comment, PI, Content</code> nodes as an XML file is
 * parsed. All optional features (pis, comments, doctypes) are
 * recorded in the tree.
 * 
 * @author sufrin
 */
public class TreeFactory implements XMLTreeFactory<Node>
{
	/** True if entities have been expanded */
	protected boolean expandedEntities = true;

	/**
	 * Construct a TreeFactory
	 * 
	 * @param expandedEntitities
	 *            -- true if entities will have been expanded before
	 *            the tree nodes are built
	 */
	public TreeFactory(boolean expandedEntities)
	{
		this.expandedEntities = expandedEntities;
	}

	/** <code>this(true)</code> */
	public TreeFactory()
	{
		this(true);
	}

	public Element newElement(String kind, XMLAttributes atts)
	{
		return new Element(kind, atts);
	}

	public Element newRoot()
	{
		return newElement("", new XMLAttrMap());
	}

	public Content newContent(String name, boolean cdata)
	{
		return new Content(name, cdata, expandedEntities);
	}

	public Node newComment(String data)
	{
		return new Comment(data);
	}

	public Node newDOCTYPE(String data)
	{
		return new DOCTYPE(data);
	}

	public Node newPI(String data)
	{
		return new PI(data);
	}

	public Node newSpaces(String text)
	{
		return new Spaces(text);
	}

	public boolean wantSpaces()
	{
		return false;
	}

	protected Map<String, String> map = new HashMap<String, String>();

	public Map<String, String> getMap()
	{
		return map;
	}

	protected XMLLocator locator;
	protected boolean wantPI = true;
	protected boolean wantComment = true;
	protected boolean literalOutput = false;
	protected boolean logDOCTYPE = false;
	protected boolean wantDOCTYPE = true;

	public void setLocator(XMLLocator locator)
	{
		this.locator = locator;
	}

	public XMLLocator getLocator()
	{
		return locator;
	}

	protected boolean literalOutput()
	{
		return literalOutput;
	}

	protected boolean logDOCTYPE()
	{
		return logDOCTYPE;
	}

	protected void setLiteralOutput(boolean literalOutput)
	{
		this.literalOutput = literalOutput;
	}

	protected void setLogDOCTYPE(boolean logDOCTYPE)
	{
		this.logDOCTYPE = logDOCTYPE;
	}

	protected void setWantComment(boolean wantComment)
	{
		this.wantComment = wantComment;
	}

	protected void setWantDOCTYPE(boolean wantDOCTYPE)
	{
		this.wantDOCTYPE = wantDOCTYPE;
	}

	protected void setWantPI(boolean wantPI)
	{
		this.wantPI = wantPI;
	}

	public boolean wantComment()
	{
		return wantComment;
	}

	public boolean wantDOCTYPE()
	{
		return wantDOCTYPE;
	}

	public boolean wantPI()
	{
		return wantPI;
	}

}
