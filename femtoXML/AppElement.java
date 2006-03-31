   package femtoXML;
   import java.util.*;
   public class AppElement implements AppTree, XMLComposite<AppTree>, Iterable<AppTree>
   { protected String                kind;
     protected Map<String,String>    attrs;
     protected Vector<AppTree> subtrees = new Vector<AppTree>();
     
     public AppTree close() { return this; }
     
     public AppElement(String kind, Map<String,String> attrs) 
     { this.kind = kind; this.attrs = attrs; }
     
     public void              addTree(AppTree t)         { subtrees.add(t); }
     public String            getKind()                  { return kind; }
     public Iterator<AppTree> iterator()                 { return subtrees.iterator(); }
     public String toString() 
     { return String.format("<%s%s>%s</%s>", kind, attrs, formatIter(subtrees), kind); }
     
     public static String formatIter(Vector<AppTree> subtrees) 
     { StringBuilder s     = new StringBuilder();
       boolean       first = true;
       for (AppTree t: subtrees) 
       { if (first) first=false; else s.append(" "); s.append(t.toString()); }
       return s.toString();
     }
      public void printTo(java.io.PrintStream out, int indent) 
      { for (int i=0; i<indent; i++) out.print(" ");                    // Indent to open bracket position
        if (subtrees.size()==0)                                         // Can we abbreviate the tree?
           out.print(String.format("<%s%s/>", kind, attrs));
        else
        {  out.print(String.format("<%s%s>", kind, attrs));
           boolean wasWord=false;                                       // Last printed tree was a Word
           for (AppTree t: subtrees)
           { boolean isWord = t instanceof AppWord; 
             boolean needNL = !wasWord || !isWord;                 
             if (needNL) out.println();
             t.printTo(out, needNL ? indent+2 : 1);
             wasWord = isWord; 
           }
           out.println(); for (int i=0; i<indent; i++) out.print(" ");  // Align close bracket with open bracket
           out.print(String.format("</%s>", kind));
        }
      }      
   }

