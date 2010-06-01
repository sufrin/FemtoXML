package femtoXML.app;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import femtoXML.FormatWriter;
import femtoXML.XMLAttrMap;
import femtoXML.XMLAttributes;
import femtoXML.XMLInputReader;
import femtoXML.XMLParser;
import femtoXML.XMLScanner;
import femtoXML.XMLSyntaxError;
import static femtoXML.app.NodePred.*;
import static femtoXML.app.Element.*;
import static femtoXML.app.Stream.*;

/**
 * $Id$
 * 
 * Example of a <code>femtoXML</code> application. Its main useful
 * function is to pretty-print its input XML files onto the standard
 * output stream, but it can also transcode files, expand internal
 * entities, etc. etc.
 * <p>
 * The femtoXML API has been simplified to the point where common
 * tasks can be accomplished straightforwardly. On the face of it this
 * might be though to compromise the versatility of the API, but this
 * application demonstrates that by appropriate use of inheritance one
 * can achieve specialised effects. An extreme example of this is the
 * subclassing of the <code>TreeFactory</code>, used below: it
 * provides an <i>ad-hoc</i> means of analysis of DTDs that supports
 * the definition of internal entities. Note the sharing of
 * <code>map</code> between the tree factory, the command-line
 * interpreter, and the <code>XMLParser.decodeEntity</code> method of
 * the parser that is passed to the <code>XMLScanner</code>
 * constructed later.
 * </p>
 * 
 */
public class App
{
	public static void main(String[] args) throws Exception
	{
		App it = new App();
		it.run(args);
	}

	/** Command-line switch state */
	boolean expandEntities = true, isAscii = false, wantENC = false,
			alignParam = true, testPath = false, rewrite = false;

	/** Command-line switch state */
	String enc = "UTF-8", ienc = null;

	/** Command-line switch state */
	int splitParam = 2;

	TreeFactory factory = new TreeFactoryWithDOCTYPE(expandEntities);

	/** Mapping from internal entity names to their expansions */
	final Map<String, String> map = factory.getMap();

	/** Tags of the elements that require spaces to be preserved */
	final Set<String> spaces = new HashSet<String>();

	/**
	 * An <code>XMLparser</code> that implements (internal) entity
	 * decoding by using the <code>map</code>; forwards
	 * <code>wantSpaces()</code> to the currently-open element; and
	 * plugs appropriate parameter values into
	 * <code>XMLAttributes</code> as they are constructed.
	 */
	XMLParser<Node> parser = new XMLParser<Node>(factory)
	{
		@Override
		public Reader decodeEntity(String name)
		{
			String value = map.get(name);
			return new StringReader(value == null ? String.format("&amp;%s;",
					name) : value);
		}

		@Override
		public boolean wantSpaces()
		{
			return stack.peek().wantSpaces();
		}

		@Override
		public XMLAttributes newAttributes(boolean expandEntitites)
		{
			XMLAttrMap map = new XMLAttrMap().setExpandedEntities(
					expandEntitites).setSplit(splitParam).setAlign(alignParam);
			return map;
		}

	};

	public void run(String[] args) throws Exception
	{ // Process the arguments on the command line
		Vector<String> files = new Vector<String>();
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if (arg.startsWith("-h"))
				System.err
						.printf("-p         -- ignore <? processing instructions%n"
								+ "-e key val -- expand &key; as val%n"
								+ "-a         -- encode Unicode characters >= 128 as entities%n"
								+ "-s tag     -- preserve spacing inside <tag markup%n"
								+ "-d         -- ignore <!DOCTYPE declarations%n"
								+ "+D         -- LOG DOCTYPE DECLARATION DETAILS%n"
								+ "-c         -- ignore comments%n"
								+ "-i         -- indent the source text without expanding entities%n"
								+ "-x         -- do not re-encode characters in content on output (to simplify some markup tests)%n"
								+ "-ns        -- resolve namespaces and normalize the tree"
								+ "-enc  enc  -- output encoding is enc (default is UTF-8)%n"
								+ "-ienc enc  -- input encoding is enc (the program deduces the encoding otherwise)%n"
								+ "-aa        -- don't bother aligning attribute values in tags%n"
								+ "-as <int>  -- show attributes on separate lines of there are more than <int> of them (default 2)%n"
								+ "-TP        -- test the path iterators features%n"
								+ "-r         -- (test) rewrite using built-in rules"
								+ "($Revision$)%n");
			else if (arg.equals("-a"))
				isAscii = true;
			else if (arg.equals("-aa"))
				alignParam = false;
			else if (arg.equals("-ns"))
				factory.setResolvingNameSpaces(true);
			else if (arg.equals("-as"))
				splitParam = Integer.parseInt(args[++i]);
			else if (arg.equals("-TP"))
				testPath = true;
			else if (arg.equals("-r"))
				rewrite = true;
			else if (arg.equals("-i"))
			{
				expandEntities = false;
				factory.setLiteralOutput(true);
			} else if (arg.equals("-x"))
			{
				factory.setLiteralOutput(true);
			} else if (arg.equals("-enc"))
			{
				enc = args[++i];
				wantENC = true;
			} else if (arg.equals("-ienc"))
				ienc = args[++i];
			else if (arg.equals("-e"))
				map.put(args[++i], args[++i]);
			else if (arg.equals("-s"))
				spaces.add(args[++i]);
			else if (arg.equals("-d"))
				factory.setWantDOCTYPE(false);
			else if (arg.equals("+D"))
				factory.setLogDOCTYPE(true);
			else if (arg.startsWith("-D"))
			{
				String[] argt = arg.substring(2).split("=", 2);
				if (argt.length == 2)
					System.setProperty(argt[0], argt[1]);
				else
					System.err.println(arg + "?");
			} else if (arg.equals("-c"))
				factory.setWantComment(false);
			else if (arg.equals("-p"))
				factory.setWantPI(false);
			else
				files.add(arg);
		}

		// Set up parser and format writer
		XMLScanner scanner = new XMLScanner(parser);
		FormatWriter out = new FormatWriter(new OutputStreamWriter(System.out,
				enc));
		scanner.setExpandEntities(expandEntities);
		out.setCharEntities(isAscii);

		// Process the input file(s)
		for (String arg : files)
		{
			try
			{
				scanner.read(new LineNumberReader(new XMLInputReader(
						new FileInputStream(arg), ienc)), arg);

				if (wantENC)
				{
					out.println(String.format(
							"<?xml version='1.1' encoding='%s'?>%n", enc));
				}
				Element root = (Element) parser.getTree();
				if (rewrite)
					doRewrites(root);
				else
					for (Node tree : root)
						tree.printTo(out, 0);
				out.println();
				out.flush();
			} catch (XMLSyntaxError ex)
			{
				System.err.printf("%s%n", ex.getMessage());
			}
		}
	}

	public Template selectBody(Pred<Node> pred)
	{
		return new Template(pred)
		{
			public Stream<Node> gen(Node elt)
			{
				return elt.body();
			}
		};
	}

	public NodeTemplate mapElement(Pred<Node> pred, final String elementName)
	{
		return new NodeTemplate(pred)
		{
			public Node genNode(Node elt)
			{
				return element(elementName).with(elt.body());
			}
		};
	}
	
  /** Equivalent to the xslt transform
	* <pre>
	* <code><!--
	*  <html>
	*	   <body>
	*	    <table border="1">
	*	    <xsl:for-each select="catalog/cd">
	*	    <tr>
	*	      <td><xsl:value-of select="title"/></td>
	*	      <td><xsl:value-of select="artist"/></td>
	*	    </tr>
	*	    </xsl:for-each>
	*	    </table>
	*	   </body>
	*	 </html>-->
	* </code>
	* </pre>
	* 
	*/
	public Node cdCatalogue(Node root)
	{
		final Template titleBody  = mapElement(isPath("title"),  "td");
		final Template artistBody = mapElement(isPath("artist"), "td");
		final Template newBody = titleBody.cat(artistBody);

		Template tabulateCDs = new NodeTemplate(isPath("catalog", "cd"))
		{  public Node genNode(Node cd) {return element("tr").with(cd.body().map(newBody)); }};

		return element("html").with(
				element("body").with(
						element("table", "border", "1").with(
								root.prefixCursor(tabulateCDs).map(tabulateCDs))));
	}

	public void doRewrites(final Node root) throws UnsupportedEncodingException
	{   FormatWriter out = new FormatWriter(new OutputStreamWriter(System.out, enc));
		cdCatalogue(root).printTo(out, 0);
		out.flush();
		out.close();
	}

}
