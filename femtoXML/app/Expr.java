package femtoXML.app;

/** An expression */
public interface Expr
{
   Value eval(Node cxt);
}
