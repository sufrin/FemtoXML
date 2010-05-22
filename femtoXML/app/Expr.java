package femtoXML.app;

/** An expression: mapping C to V */
public interface Expr<C,V>
{
   V eval(C cxt);
}
