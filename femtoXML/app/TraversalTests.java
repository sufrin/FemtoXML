package femtoXML.app;

import static femtoXML.app.NodePred.below;
import static femtoXML.app.NodePred.containing;
import static femtoXML.app.NodePred.hasAttr;
import static femtoXML.app.NodePred.isElement;
import static femtoXML.app.NodePred.isElementMatching;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import femtoXML.FormatWriter;

public class TraversalTests
{
	// ///////////////////////// PATH FEATURES TESTBED
	// /////////////////////////

	/*
	 * Various traversals
	 */
	public void testPathFeatures(Node t) throws UnsupportedEncodingException
	{
		FormatWriter out = new FormatWriter(new OutputStreamWriter(System.out,
				"utf8"));
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
