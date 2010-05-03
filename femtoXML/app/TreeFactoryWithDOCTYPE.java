/**
 * 
 */
package femtoXML.app;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import femtoXML.XMLAttributes;
import femtoXML.XMLCharUtil;
import femtoXML.XMLSyntaxError;
/**
 * A tree factory that processes entity declarations in DTDs. <br/>
 * <b>WARNING:</b> the algorithm used here is ad-hoc and incomplete: whilst we expand
 * parameter entities in entity declarations, we do not treat any material outside
 * entity declarations. 
 * <p>
 * Internal entity definitions are placed in the <code>map</code> that is shared by the <code>XMLParser parser</code>
 * defined below.
 * </p>
 * 
 */
public class TreeFactoryWithDOCTYPE extends TreeFactory {

	private boolean wantPI = true;

	private boolean wantComment = true;

	private boolean literalOutput = false;

	private boolean logDOCTYPE = false;

	private boolean wantDOCTYPE = true;

	Pattern entity = Pattern
			.compile(
					"<!ENTITY\\s+([%]{0,1})\\s*([A-Za-z0-9:_]+)\\s+(PUBLIC|SYSTEM|)\\s*((\"([^\"]*)\")|(\'([^\']*)\'))\\s*((\"([^\"]+)\")|(\'([^\']+)\'))?\\s*>",
					Pattern.MULTILINE);
	Pattern paramref = Pattern.compile("%([A-Za-z0-9:_]+);", Pattern.MULTILINE);
	Pattern dtd = Pattern
			.compile(
					"\\s*([A-Za-z0-9:_]+)\\s*((PUBLIC|SYSTEM)\\s*((\"([^\"]*)\")|(\'([^\']*)\'))\\s*((\"([^\"]+)\")|(\'([^\']+)\'))?)?\\s*(\\[(.*)\\])?\\s*",
					Pattern.DOTALL);
	// http://www.w3.org/TR/REC-xml/#sec-entexpand suggests that only &#...; are
	// expanded during entity definition
	// (this doesn't seem completely right to me, but standards is standards!)
	Pattern charref = Pattern.compile("&(#[A-Fa-f0-9:_]+);", Pattern.MULTILINE);

	final Map<String, String> pmap = new HashMap<String, String>();

	private Map<String, String> map = new HashMap<String, String>();

	public TreeFactoryWithDOCTYPE(boolean expandedEntities) {
		super(expandedEntities);
	}

	/** Expand character references in <code>value</code> */
	String expandCharRefs(String value) {
		int start = 0;
		Matcher m = charref.matcher(value);
		StringBuilder b = new StringBuilder();
		while (m.find(start)) {
			String pid = m.group(1);
			char c = XMLCharUtil.decodeCharEntity(pid);
			b.append(value.substring(start, m.start()));
			if (c == '\000')
				b.append("&" + pid + ";");
			else
				b.append(c);
			start = m.end();
		}
		b.append(value.substring(start));
		return b.toString();
	}

	String expandParamRefs(String value) {
		int start = 0;
		Matcher m = paramref.matcher(value);
		StringBuilder b = new StringBuilder();
		while (m.find(start)) {
			String pid = m.group(1);
			String val = pmap.get(pid);
			if (val == null)
				val = "%" + pid + ";";
			b.append(value.substring(start, m.start()));
			b.append(val);
			start = m.end();
		}
		b.append(value.substring(start));
		return b.toString();
	}

	Map<String, String> getMap() {
		return map;
	}

	protected boolean literalOutput() {
		return literalOutput;
	}

	protected boolean logDOCTYPE() {
		return logDOCTYPE;
	}

	@Override
	public Content newContent(String data, boolean cdata) {
		return new Content(data, cdata, !literalOutput);
	}

	@Override
	public Node newDOCTYPE(String data) {
		Matcher d = dtd.matcher(data);
		if (d.lookingAt()) {
			String nameDTD = d.group(1);
			String systemDTD = d.group(3);
			boolean isPublicDTD = "PUBLIC".equalsIgnoreCase(systemDTD);
			@SuppressWarnings("unused")
			boolean isSystemDTD = "SYSTEM".equalsIgnoreCase(systemDTD)
					|| isPublicDTD;
			String dtd1 = d.group(6);
			String dtd2 = d.group(11);
			if (dtd1 == null)
				dtd1 = d.group(8);
			if (dtd2 == null)
				dtd2 = d.group(13);
			if (logDOCTYPE())
				System.err.printf("DTD %s %s %s %s%n", nameDTD,
						systemDTD == null ? "" : systemDTD, dtd1 == null ? ""
								: dtd1, dtd2 == null ? "" : dtd2);
		} else
			throw new XMLSyntaxError(getLocator(),
					"DOCTYPE declaration malformed");

		String internal = d.group(15);
		if (internal != null)
			processDTD(internal);
		return new DOCTYPE(data);
	}

	@Override
	public Element newElement(String kind, XMLAttributes atts) { // System.err.println(kind);
		return super.newElement(kind, atts);
	}

	public void processDTD(String data) {
		int start = 0;
		Matcher m = entity.matcher(data);
		StringBuilder errors = new StringBuilder();
		start = 0;
		while (m.find(start)) {
			boolean isPE = m.group(1).equals("%");
			String name = m.group(2);
			String system = m.group(3);
			boolean isPublic = system.equalsIgnoreCase("PUBLIC");
			boolean isSystem = system.equalsIgnoreCase("SYSTEM") || isPublic;
			String value = m.group(6);
			String value2 = m.group(11);
			if (value == null)
				value = m.group(8);
			if (value2 == null)
				value2 = m.group(13);
			// Sanity check
			if (!isPublic && value2 != null)
				errors
						.append(String.format(
								"Malformed SYSTEM entity declaration %s%n", m
										.group(0)));
			if (isPublic && value2 == null)
				errors
						.append(String.format(
								"Malformed PUBLIC entity declaration %s%n", m
										.group(0)));
			// For the moment we will only look at internal entities
			if (!isSystem) {
				value = expandParamRefs(expandCharRefs(value)); // XML standard
																// is weird
				(isPE ? pmap : getMap()).put(name, value);
			} else {
				System.err.printf("Warning: [not yet implemented] %s%n", m
						.group(0));
			}
			if (logDOCTYPE())
				System.err.printf("ENTITY %s%s %s = %s [%s]%n",
						isPE ? "%" : "", system, name, value,
						value2 == null ? "" : value2);
			start = m.end();
		}
		if (errors.length() > 0)
			throw new XMLSyntaxError(getLocator(), errors.toString());

	}

	protected void setLiteralOutput(boolean literalOutput) {
		this.literalOutput = literalOutput;
	}

	protected void setLogDOCTYPE(boolean logDOCTYPE) {
		this.logDOCTYPE = logDOCTYPE;
	}

	protected void setWantComment(boolean wantComment) {
		this.wantComment = wantComment;
	}

	protected void setWantDOCTYPE(boolean wantDOCTYPE) {
		this.wantDOCTYPE = wantDOCTYPE;
	}

	protected void setWantPI(boolean wantPI) {
		this.wantPI = wantPI;
	}

	@Override
	public boolean wantComment() {
		return wantComment;
	}

	@Override
	public boolean wantDOCTYPE() {
		return wantDOCTYPE;
	}


	@Override
	public boolean wantPI() {
		return wantPI;
	}
}