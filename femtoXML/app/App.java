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

/**
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
 * @author sufrin ($Revision$)
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
				if (testPath)
					testPathFeatures(root);
				else
					for (Node tree : root)
						tree.copy().printTo(out, 0);
				out.println();
				out.flush();
			} catch (XMLSyntaxError ex)
			{
				System.err.printf("%s%n", ex.getMessage());
			}
		}
	}
	
	public void doRewrites(final Node root) throws UnsupportedEncodingException
	{
		FormatWriter out = new FormatWriter(new OutputStreamWriter(System.out, enc));
		
		/**
		 *  This rule matches <article> ... <author> author details </author> ... </article>
		 *  and rewrites it as
		 *  <blog> <writer> author details </writer> ... ... </blog>
		 */
		Rule rule1 = new Rule(isElementMatching("article"))
		{ public Node eval(Node article)
		  { Cursor<Node> authElement = article.body().filter(isElementMatching("author"));
		    for (Node author: authElement)
		        return element("blog")
                       .with(element("writer").with(author.body()))
		               .with(article.body().filter(notEqual(author)));
		    return null;
		  }			
		};
		
		Rule rule2 = new Rule(isElementMatching("entry"))
		{
			public Node eval(Node entry)
			  {     Pred<Node> date = isElementMatching("date");
			        return element("blogEntry")
	                       .with(element("dated").with(entry.body().filter(date))
			               .with(entry.body().filter(date.not())));
			  }		
		};
		
		Rule rule = rule1.orElse(rule2);
		
		
	    // for (Node v : root.breadthCursor(rule.getGuard())) { out.println(); v.printTo(out, 0); out.println("\n------------"); }

		out.println("\n------------"); 
		/**
		 *  This applies the rule to all the nodes read from the input
		 */
		for (Node node : root.breadthCursor(rule.getGuard()))
		{
			Value v = rule.apply(node);
			if (v==null) continue;
			v.printTo(out, 0);
		}
		out.flush();
		out.close();
	}


	// ///////////////////////// PATH FEATURES TESTBED
	// /////////////////////////

	/*
	 * Various traversals
	 */
	public void testPathFeatures(Node t) throws UnsupportedEncodingException
	{
		FormatWriter out = new FormatWriter(new OutputStreamWriter(System.out,
				enc));
		// Statistics for the caching: count all the tree nodes
		long nodes = 0;
		for (@SuppressWarnings("unused") Node node : t.prefixCursor())
			nodes++;

		// //////////////////////////////////////// Containment test
		Pred.Cached<Node> cont = below(isElementMatching("hor.*"), 1).and(
				containing(hasAttr("group", "border.*"))).cache();
		for (Node node : t.prefixCursor(cont).filter(cont))
		{
			node.printTo(out, 0);
			out.println();
		}
		out.flush();
		System.err.printf("With cutoff: nodes: %d; inspected: %d; missed %d%n",
				nodes, cont.hits, cont.cachemisses);

		// //////////////////////////////////////// Below tests
		Pred.Cached<Node> pred = below(isElementMatching("col"), 1).and(
				isElement()).cache();
		for (Node node : t.prefixCursor().filter(pred))
		{
			node.printTo(out, 0);
			out.println();
		}
		out.flush();
		System.err
				.printf(
						"Without cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n",
						nodes, pred.hits, pred.cachemisses);

		pred.hits = pred.cachemisses = 0;
		for (Node node : t.prefixCursor(pred).filter(pred))
		{
			node.printTo(out, 0);
			out.println();
		}
		out.flush();
		// Statistics for the cacheing
		System.err
				.printf(
						"With cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n",
						nodes, pred.hits, pred.cachemisses);

		pred.hits = pred.cachemisses = 0;
		// filtered without cutoff
		for (Node node : t.breadthCursor().filter(pred))
		{
			node.printTo(out, 0);
			out.println();
		}
		out.flush();
		System.err
				.printf(
						"Without cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n",
						nodes, pred.hits, pred.cachemisses);

		pred.hits = pred.cachemisses = 0;
		for (Node node : t.breadthCursor(pred).filter(pred))
		{
			node.printTo(out, 0);
			out.println();
		}
		out.flush();
		// Statistics for the cacheing
		System.err
				.printf(
						"With cutoff: nodes: %d; inspected: %d; missed %d%n------------------%n",
						nodes, pred.hits, pred.cachemisses);
	}

	public void testPathFeaturesBasic(Node t)
	{
		testRecursive(t);
		System.out.println("-------------------------");
		for (Node node : t.prefixCursor())
		{
			System.out.printf("%20s    ", (node.isElement() ? "<"
					+ ((Element) node).getKind() : node.toString()));
			for (Node s : t.pathToRoot())
				System.out.print(s.elementName() + "/");
			System.out.println();
		}
		System.out.println("-------------------------");
		for (Node node : t.breadthCursor())
		{
			System.out.printf("%20s    ", (node.isElement() ? "<"
					+ ((Element) node).getKind() : node.toString()));
			for (Node s : node.pathToRoot())
				System.out.print(s.elementName() + "/");
			System.out.println();
		}
	}

	public void testRecursive(Node t)
	{
		if (t.isElement())
		{
			for (Node s : t.pathToRoot())
				System.out.print(s.elementName() + "/");
			System.out.println();
			for (Node subtree : t)
				testRecursive(subtree);
		}

	}

}
