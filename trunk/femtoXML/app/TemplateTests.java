package femtoXML.app;

import static femtoXML.app.Element.element;
import static femtoXML.app.NodePred.TRUE;
import static femtoXML.app.NodePred.isElementMatching;
import static femtoXML.app.NodePred.notEqual;
import static femtoXML.app.Stream.flatten;

public class TemplateTests
{
	/** The value of a named attribute of a target; or a default value if there's no such attribute. */
	public static Expr<Node,String> attrExpr(final String attr, final String defValue)
	{   return new Expr<Node,String>()
		{   public String eval(Node target)
			{  String result = target.getAttr(attr);
		       return result==null ? defValue : result;
			}
		};
	}


	/**
	 *  This template matches <article> ... <author> author details </author> ... </article>
	 *  and rewrites it as
	 *  <blog> <writer> author details </writer> ... ... </blog>
	 */
	public final static Template template1 = new NodeTemplate(isElementMatching("article"))
	{ public Node genNode(Node article)
	  { for (Node author: article.body().filter(isElementMatching("author")))
	        return element("blog")
                   .with(element("writer").with(author.body()))
	               .with(article.body().filter(notEqual(author)));
	    return null;
	  }			
	};

	public final static Expr<Node, String> dateAttr = attrExpr("date", "unknown");

	/** Transforms <date>...</date> into <dated>...</dated> */
	public static final Template dateRule = new NodeTemplate(isElementMatching("date"))
	{  public Node genNode(Node dated) { return element("dated").with(dated.body()); }};
	
	public static final Template defaultDate = new NodeTemplate(TRUE)
	{  public Node genNode(Node target) { return element("dated").with(new Content(dateAttr.eval(target))); }};
	
	
	public static final Template template2 = new NodeTemplate(isElementMatching("entry"))
	{  
	   public Node genNode(Node entry)
		  {     return element("blogEntry")
                       .withFirst(entry.body().map(dateRule).cat(entry.body().map(defaultDate)))
		               .with(entry.body().filter(dateRule.not()));
		  }		
	};

	public static final Template template3 = new NodeTemplate(isElementMatching("entry"))
	{  
	   public Node genNode(Node entry)
		  {     for (Node date : flatten(entry.body().map(dateRule).cat(entry.body().map(defaultDate))))
		        return element("blogEntry")
                       .with(date)
		               .with(entry.body().filter(dateRule.not()));
		        return null;
		  }		
	};
	
	public static final Template template = template1.orElse(template2);
	
}
