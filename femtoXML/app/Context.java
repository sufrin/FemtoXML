package femtoXML.app;

public interface Context
{
   Node    currentNode();
   Value   get(String name);
   void    put(String name, Value v);
   Context newContext();
}
