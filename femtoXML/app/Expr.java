package femtoXML.app;

/** An expression */
public interface Expr<V>
{
   V eval(Node cxt);
}
