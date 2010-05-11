package femtoXML.app;

import femtoXML.FormatWriter;

public interface Value
{
	/**
	 * Write this value on the given writer at the given
	 * indentation in a form suitable for re-reading. If
	 * <code>indent&lt;=0</code> then don't use any indentation at
	 * all.
	 */
	void printTo(FormatWriter out, int indent);
}
