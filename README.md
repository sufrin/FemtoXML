FemtoXML -- XML analysis infrastructure
=======================================

FemtoXML is an ultralightweight framework for analysing XML texts. It
was designed to circumvent some of the awkwardness I found when using
the SAX and SAX2 APIs.

Lightness is achieved at the following cost:

-   The internal structure of XML syntactic features that do not appear
    in all XML documents (Processing instructions and document type
    declarations) is ignored, and their text passed to handlers
    unscrutinised.
-   An XML declaration: `<?xml ... version="..." charset="..."?>` is
    treated in the same way as any other processing instruction.

This cost is reasonable: anybody who wants to further analyse processing
instructions and DOCTYPE declarations can easily write specialised
processors. An example application (a prettyprinter/recoder)
demonstrates how straightforward DOCTYPE declarations can be processed
in order to record their entity declarations.

The information given in syntax-error messages is fairly good: the input
file and line on which the error was discovered are given, together with
the names of any entities that are currently being expanded.

Character-entity expansion is table-driven, and therefore flexible. The
basic named entities are built-in, and other named character entities
can be added to the tables.

XML analysis can be performed with or without entity expansion, thus it
is fairly easy to build an XML ''prettifier''. In fact the main example
application we provide is a prettifier that constructs a complete
xml parse tree before outputting it.

Bernard Sufrin, March 2006

