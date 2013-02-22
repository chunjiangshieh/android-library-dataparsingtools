package com.xcj.android.dat.xml.parse;

/**
 * The main XML Parser class.
 */

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

public class XMLParser {

	/**
	 * Return value of getType before first call to next()
	 */
	static final int START_DOCUMENT = 0;

	/**
	 * Signal logical end of xml document
	 */
	static final int END_DOCUMENT = 1;

	/**
	 * Start tag was just read
	 */
	static final int START_TAG = 2;

	/**
	 * End tag was just read
	 */
	static final int END_TAG = 3;

	/**
	 * Text was just read
	 */
	static final int TEXT = 4;

	static final int CDSECT = 5;

	static final int ENTITY_REF = 6;

	static final int LEGACY = 999;

    static final private String UNEXPECTED_EOF =
        "Unexpected EOF";
    
	// general

	public boolean relaxed;
	private Hashtable entityMap;
	private int depth;
	private String[] elementStack = new String[4];

	// source

	private Reader reader;
	private boolean allowEntitiesInAttributes;

	private char[] srcBuf = new char[Runtime.getRuntime().freeMemory() >= 1048576 ? 8192
			: 128];

	private int srcPos;
	private int srcCount;

	private boolean eof;

	private int line;
	private int column;

	private int peek0;
	private int peek1;

	// txtbuffer

	private char[] txtBuf = new char[128];
	private int txtPos;

	// Event-related

	private int type;
	private String text;
	private boolean isWhitespace;
	private String name;

	private boolean degenerated;
	private int attributeCount;
	private String[] attributes = new String[16];

	private String[] TYPES = { "Start Document", "End Document", "Start Tag",
			"End Tag", "Text" };

	private final int read() throws IOException {

		int r = this.peek0;
		this.peek0 = this.peek1;

		if (this.peek0 == -1) {
			this.eof = true;
			return r;
		} else if (r == '\n' || r == '\r') {
			this.line++;
			this.column = 0;
			if (r == '\r' && this.peek0 == '\n')
				this.peek0 = 0;
		}
		this.column++;

		if (this.srcPos >= this.srcCount) {
			this.srcCount = this.reader
					.read(this.srcBuf, 0, this.srcBuf.length);
			if (this.srcCount <= 0) {
				this.peek1 = -1;
				return r;
			}
			this.srcPos = 0;
		}

		this.peek1 = this.srcBuf[this.srcPos++];
		return r;
	}

	private final void exception(String desc) throws IOException {
		throw new IOException(desc + " pos: " + getPositionDescription());
	}

	private final void push(int c) {
		if (c == 0)
			return;

		if (this.txtPos == this.txtBuf.length) {
			char[] bigger = new char[this.txtPos * 4 / 3 + 4];
			System.arraycopy(this.txtBuf, 0, bigger, 0, this.txtPos);
			this.txtBuf = bigger;
		}

		this.txtBuf[this.txtPos++] = (char) c;
	}

	private final void read(char c) throws IOException {
		if (read() != c) {
			if (this.relaxed) {
				if (c <= 32) {
					skip();
					read();
				}
			} else {
				exception("expected: '" + c + "'");
			}
		}
	}

	private final void skip() throws IOException {

		while (!this.eof && this.peek0 <= ' ')
			read();
	}

	private final String pop(int pos) {
		String result = new String(this.txtBuf, pos, this.txtPos - pos);
		this.txtPos = pos;
		return result;
	}

	private final String readName() throws IOException {

		int pos = this.txtPos;
		int c = this.peek0;
		if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && c != '_'
				&& c != ':' && !this.relaxed)
			exception("name expected");

		do {
			push(read());
			c = this.peek0;
		} while ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9') || c == '_' || c == '-' || c == ':'
				|| c == '.');

		return pop(pos);
	}

	private final void parseLegacy(boolean push) throws IOException {

		String req = "";
		int term;

		read(); // <
		int c = read();

		if (c == '?') {
			term = '?';
		} else if (c == '!') {
			if (this.peek0 == '-') {
				req = "--";
				term = '-';
			} else if (this.peek0 == '[') {
				// TODO hack for <![CDATA[]]
				req = "[CDATA[";
				term = ']';
				this.type = TEXT;
				push = true;
			} else {
				req = "DOCTYPE";
				term = -1;
			}
		} else {
			if (c != '[')
				exception("cantreachme: " + c);
			req = "CDATA[";
			term = ']';
		}

		for (int i = 0; i < req.length(); i++)
			read(req.charAt(i));

		if (term == -1)
			parseDoctype();
		else {
			while (true) {
				if (this.eof)
					exception(UNEXPECTED_EOF);

				c = read();
				if (push)
					push(c);

				if ((term == '?' || c == term) && this.peek0 == term
						&& this.peek1 == '>')
					break;
			}
			read();
			read();

			if (push && term != '?')
				pop(this.txtPos - 1);
		}
	}

	/** precondition: &lt! consumed */

	private final void parseDoctype() throws IOException {

		int nesting = 1;

		while (true) {
			int i = read();
			switch (i) {

			case -1:
				exception(UNEXPECTED_EOF);
				break;

			case '<':
				nesting++;
				break;

			case '>':
				if ((--nesting) == 0)
					return;
				break;
			}
		}
	}

	/* precondition: &lt;/ consumed */

	private final void parseEndTag() throws IOException {

		read(); // '<'
		read(); // '/'
		this.name = readName();
		if (this.depth == 0 && !this.relaxed)
			exception("element stack empty");

		if (this.name.equals(this.elementStack[this.depth - 1]))
			this.depth--;
		else if (!this.relaxed)
			exception("expected: " + this.elementStack[this.depth]);
		skip();
		read('>');
	}

	private final int peekType() {
		switch (this.peek0) {
		case -1:
			return END_DOCUMENT;
		case '&':
			return ENTITY_REF;
		case '<':
			switch (this.peek1) {
			case '/':
				return END_TAG;
			case '[':
				return CDSECT;
			case '?':
			case '!':
				return LEGACY;
			default:
				return START_TAG;
			}
		default:
			return TEXT;
		}
	}

	private static final String[] ensureCapacity(String[] arr, int required) {
		if (arr.length >= required)
			return arr;
		String[] bigger = new String[required + 16];
		System.arraycopy(arr, 0, bigger, 0, arr.length);
		return bigger;
	}

	/** Sets name and attributes */

	private final void parseStartTag() throws IOException {

		read(); // <
		this.name = readName();
		this.elementStack = ensureCapacity(this.elementStack, this.depth + 1);
		this.elementStack[this.depth++] = this.name;

		while (true) {
			skip();

			int c = this.peek0;

			if (c == '/') {
				this.degenerated = true;
				read();
				skip();
				read('>');
				break;
			}

			if (c == '>') {
				read();
				break;
			}

			if (c == -1)
				exception(UNEXPECTED_EOF);

			String attrName = readName();

			if (attrName.length() == 0)
				exception("attr name expected");

			skip();
			read('=');

			skip();
			int delimiter = read();

			if (delimiter != '\'' && delimiter != '"') {
				if (!this.relaxed)
					exception("<" + this.name + ">: invalid delimiter: "
							+ (char) delimiter);

				delimiter = ' ';
			}

			int i = (this.attributeCount++) << 1;

			this.attributes = ensureCapacity(this.attributes, i + 4);

			this.attributes[i++] = attrName;

			int p = this.txtPos;

			if (this.allowEntitiesInAttributes) {
				pushText(delimiter);
			} else {
				pushTextAttribute(delimiter);
			}

			this.attributes[i] = pop(p);

			if (delimiter != ' ')
				read(); // skip endquote
		}
	}

	/**
	 * result: isWhitespace; if the setName parameter is set, the name of the
	 * entity is stored in "name"
	 */

	public final boolean pushEntity() throws IOException {

		read(); // &

		int pos = this.txtPos;

		while (!this.eof && this.peek0 != ';') {
			push(read());
		}

		String code = pop(pos);

		read();

		if (code.length() > 0 && code.charAt(0) == '#') {
			int c = (code.charAt(1) == 'x' ? Integer.parseInt(
					code.substring(2), 16) : Integer
					.parseInt(code.substring(1)));
			push(c);
			return c <= ' ';
		}

		String result = (String) this.entityMap.get(code);
		boolean whitespace = true;

		if (result == null) {
			result = "&" + code + ";";
		}

		for (int i = 0; i < result.length(); i++) {
			char c = result.charAt(i);
			if (c > ' ') {
				whitespace = false;
			}
			push(c);
		}

		return whitespace;
	}

/** types:
'<': parse to any token (for nextToken ())
'"': parse to quote
' ': parse to whitespace or '>'
*/

	private final boolean pushText(int delimiter) throws IOException {

		boolean whitespace = true;
		int next = this.peek0;

		while (!this.eof && next != delimiter) { // covers eof, '<', '"'

			if (delimiter == ' ')
				if (next <= ' ' || next == '>')
					break;

			if (next == '&') {
				if (!pushEntity())
					whitespace = false;

			} else {
				if (next > ' ')
					whitespace = false;

				push(read());
			}

			next = this.peek0;
		}

		return whitespace;
	}

	private final boolean pushTextAttribute(int delimiter) throws IOException {
		boolean whitespace = true;
		int next = this.peek0;

		while (!this.eof && next != delimiter) { // covers eof, '<', '"'

			if (delimiter == ' ')
				if (next <= ' ' || next == '>')
					break;

			if (next > ' ')
				whitespace = false;

			push(read());
			next = this.peek0;
		}

		return whitespace;
	}

	// --------------- public part starts here... ---------------

	public void defineCharacterEntity(String entity, String value) {
		this.entityMap.put(entity, value);
	}

	public int getDepth() {
		return this.depth;
	}

	public String getPositionDescription() {

		StringBuffer buf = new StringBuffer(
				this.type < this.TYPES.length ? this.TYPES[this.type] : "Other");

		buf.append(" @" + this.line + ":" + this.column + ": ");

		if (this.type == START_TAG || this.type == END_TAG) {
			buf.append('<');
			if (this.type == END_TAG)
				buf.append('/');

			buf.append(this.name);
			buf.append('>');
		} else if (this.isWhitespace)
			buf.append("[whitespace]");
		else
			buf.append(getText());

		return buf.toString();
	}

	public int getLineNumber() {
		return this.line;
	}

	public int getColumnNumber() {
		return this.column;
	}

	public boolean isWhitespace() {
		return this.isWhitespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#getText()
	 */
	public String getText() {

		if (this.text == null)
			this.text = pop(0);

		return this.text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#getName()
	 */
	public String getName() {
		return this.name;
	}

	public boolean isEmptyElementTag() {
		return this.degenerated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#getAttributeCount()
	 */
	public int getAttributeCount() {
		return this.attributeCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#getAttributeName(int)
	 */
	public String getAttributeName(int index) {
		if (index >= this.attributeCount) {
			throw new IndexOutOfBoundsException();
		}
		return this.attributes[index << 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#getAttributeValue(int)
	 */
	public String getAttributeValue(int index) {
		if (index >= this.attributeCount) {
			throw new IndexOutOfBoundsException();
		}
		return this.attributes[(index << 1) + 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.enough.polish.xml.SimplePullParser#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String attrName) {

		for (int i = (this.attributeCount << 1) - 2; i >= 0; i -= 2) {
			if (this.attributes[i].equals(attrName))
				return this.attributes[i + 1];
		}

		return null;
	}

	public int getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.enough.polish.xml.SimplePullParser#next()
	 */
	public int next() {

		try {

			if (this.degenerated) {
				this.type = END_TAG;
				this.degenerated = false;
				this.depth--;
				return this.type;
			}

			this.txtPos = 0;
			this.isWhitespace = true;

			do {
				this.attributeCount = 0;

				this.name = null;
				this.text = null;
				this.type = peekType();

				switch (this.type) {

				case ENTITY_REF:
					this.isWhitespace &= pushEntity();
					this.type = TEXT;
					break;

				case START_TAG:
					parseStartTag();
					break;

				case END_TAG:
					parseEndTag();
					break;

				case END_DOCUMENT:
					break;

				case TEXT:
					this.isWhitespace &= pushText('<');
					break;

				case CDSECT:
					parseLegacy(true);
					this.isWhitespace = false;
					this.type = TEXT;
					break;

				default:
					parseLegacy(false);
				}
			} while (this.type > TEXT || this.type == TEXT
					&& peekType() >= TEXT);

			this.isWhitespace &= this.type == TEXT;

		} catch (IOException e) {
			this.type = END_DOCUMENT;
		}

		return this.type;
	}

	// -----------------------------------------------------------------------------
	// utility methods to mak XML parsing easier ...

	/**
	 * test if the current event is of the given type and if the name do match.
	 * null will match any namespace and any name. If the current event is TEXT
	 * with isWhitespace()= true, and the required type is not TEXT, next () is
	 * called prior to the test. If the test is not passed, an exception is
	 * thrown. The exception text indicates the parser position, the expected
	 * event and the current event (not meeting the requirement.
	 * 
	 * <p>
	 * essentially it does this
	 * 
	 * <pre>
	 *  if (getType() == TEXT && type != TEXT && isWhitespace ())
	 *    next ();
	 * 
	 *  if (type != getType
	 *  || (name != null && !name.equals (getName ())
	 *     throw new XmlPullParserException ( "....");
	 * </pre>
	 */
	public void require(int eventType, String eventName) throws IOException {

		if (this.type == TEXT && eventType != TEXT && isWhitespace()) {
			next();
		}

		if (eventType != this.type
				|| (eventName != null && !eventName.equals(getName()))) {
			exception("expected: " + this.TYPES[eventType] + "/" + eventName);
		}
	}

	/**
	 * If the current event is text, the value of getText is returned and next()
	 * is called. Otherwise, an empty String ("") is returned. Useful for
	 * reading element content without needing to performing an additional check
	 * if the element is empty.
	 * 
	 * <p>
	 * essentially it does this
	 * 
	 * <pre>
	 *   if (getType != TEXT) return ""
	 *    String result = getText ();
	 *    next ();
	 *    return result;
	 * </pre>
	 */

	public String readText() throws IOException {

		if (this.type != TEXT)
			return "";

		String result = getText();
		next();
		return result;
	}

//	private InputStream inputReader;
//
//	/**
//	 * The handler for XML Events.
//	 */
//
//	private XMLEventListener eventHandler;
//
//	/**
//	 * The root tag for the document.
//	 */
//
//	private String rootTag = null;
//
//	/**
//	 * Flag to say whether or not this stream is UTF-8 encoded.
//	 */
//
	private boolean isUTF8Encoded;
//
//	/**
//	 * The input stream being read.
//	 */
//
//	// private DataReadStream is;
//	/**
//	 * Flag to say whether or not all tags should be converted to lower case
//	 */
//
//	private boolean convertTagsToLowerCase;

	/**
	 * Constructor, Used to override default dispatcher.
	 * 
	 * @param _eventHandler
	 *            The event handle to dispatch events through.
	 * @throws IOException 
	 */

	public XMLParser(Reader reader) throws IOException {
		this(reader, true);
	}

	public XMLParser(Reader reader, boolean allowEntitiesInAttributes)
			throws IOException {
		this.reader = reader;
		this.allowEntitiesInAttributes = allowEntitiesInAttributes;

		this.peek0 = reader.read();
		this.peek1 = reader.read();

		this.eof = this.peek0 == -1;

		this.entityMap = new Hashtable();
		this.entityMap.put("amp", "&");
		this.entityMap.put("apos", "'");
		this.entityMap.put("gt", ">");
		this.entityMap.put("lt", "<");
		this.entityMap.put("quot", "\"");

		this.line = 1;
		this.column = 1;
	}

	
//	public XMLParser(XMLEventListener _eventHandler) throws IOException {
//		eventHandler = _eventHandler;
//		convertTagsToLowerCase = true;
//
//		this.entityMap = new Hashtable();
//		this.entityMap.put("amp", "&");
//		this.entityMap.put("apos", "'");
//		this.entityMap.put("gt", ">");
//		this.entityMap.put("lt", "<");
//		this.entityMap.put("quot", "\"");
//		
//		this.allowEntitiesInAttributes = true;
//
//		this.peek0 = reader.read();
//		this.peek1 = reader.read();
//
//		this.eof = this.peek0 == -1;
//
//
//		this.line = 1;
//		this.column = 1;
//
//	}

//	/**
//	 * Method to indicate if all tags should be converted to lower case
//	 * 
//	 * @param doConversion
//	 *            Whether or not to convert all tag names to lower case.
//	 */
//
//	public void convertAllTagNamesToLowerCase(boolean doConversion) {
//		convertTagsToLowerCase = doConversion;
//	}
//
//	/**
//	 * Method to set the flag to state whether or not the input is UTF-8
//	 * encoded. For the UTF-8 decoding to work the parse method MUST be called
//	 * by passing it a java.io.DataInputStream object.
//	 * 
//	 * @param flag
//	 *            True if UTF-8 decoding should be performed on the input
//	 *            stream, false if not.
//	 */
//
	public void setInputUTF8Encoded(boolean flag) {
		isUTF8Encoded = flag;
	}
//
//	/**
//	 * Method to get the next character from the input stream.
//	 */
//
//	public int getNextCharacter() throws Exception {
//		int actualValue = -1;
//
//		int inputValue = inputReader.read();
//		if (inputValue == -1)
//			return -1;
//
//		// Single character
//		if (isUTF8Encoded == false) {
//			actualValue = inputValue;
//		} else {
//			actualValue = inputValue;
//			/*
//			 * inputValue &= 0xff; if ((inputValue & 0x80) == 0) { actualValue =
//			 * inputValue; } else if ((inputValue & 0xF8) == 0xF0) {
//			 * System.out.println("1"); actualValue = (inputValue & 0x1f) << 6;
//			 * System.out.println("2"); int nextByte = inputReader.read() &
//			 * 0xff; if ((nextByte & 0xC0) != 0x80) throw new
//			 * Exception("Invalid UTF-8 format"); actualValue += (nextByte &
//			 * 0x3F) << 6; System.out.println("3"); nextByte =
//			 * inputReader.read() & 0xff; if ((nextByte & 0xC0) != 0x80) throw
//			 * new Exception("Invalid UTF-8 format"); actualValue += (nextByte &
//			 * 0x3F) << 6; System.out.println("4"); nextByte =
//			 * inputReader.read() & 0xff; if ((nextByte & 0xC0) != 0x80) throw
//			 * new Exception("Invalid UTF-8 format"); actualValue += (nextByte &
//			 * 0x3F); } else if ((inputValue & 0xF0) == 0xE0) { actualValue =
//			 * (inputValue & 0x1f) << 6; System.out.println("5"); int nextByte =
//			 * inputReader.read() & 0xff; if ((nextByte & 0xC0) != 0x80) throw
//			 * new Exception("Invalid UTF-8 format"); actualValue += (nextByte &
//			 * 0x3F) << 6; System.out.println("6"); nextByte =
//			 * inputReader.read() & 0xff; if ((nextByte & 0xC0) != 0x80) throw
//			 * new Exception("Invalid UTF-8 format"); actualValue += (nextByte &
//			 * 0x3F); } else if ((inputValue & 0xE0) == 0xC0) { actualValue =
//			 * (inputValue & 0x1f) << 6; System.out.println("7"); int nextByte =
//			 * inputReader.read() & 0xff; if ((nextByte & 0xC0) != 0x80) {
//			 * System.out.println("xxxx"); throw new
//			 * Exception("Invalid UTF-8 format"); }
//			 * 
//			 * System.out.println("8"); actualValue += (nextByte & 0x3F);
//			 * 
//			 * }
//			 */
//		}
//
//		return actualValue;
//	}
//
//	/**
//	 * Method to read until an end condition.
//	 * 
//	 * @param endChar
//	 *            The character to stop reading on
//	 * @return A string representation of the data read.
//	 */
//	ByteArrayOutputStream tempStream;

//	private String readUntilEnd(char endChar) throws Exception {
//
//		// StringBuffer data = new StringBuffer();
//		if (tempStream == null) {
//			tempStream = new ByteArrayOutputStream();
//		} else {
//			tempStream.reset();
//		}
//
//		int nextChar = getNextCharacter();
//
//		if (nextChar == -1)
//			return null;
//
//		// throw new EndOfXMLException();
//		while (nextChar != -1 && nextChar != endChar) {
//			tempStream.write(nextChar);
//
//			// data.append((char) nextChar);
//			nextChar = getNextCharacter();
//			// System.out.println("333");
//		}
//
//		if (nextChar != '<' && nextChar != '>')
//			tempStream.write(nextChar);
//		// data.append((char) nextChar);
//
//		byte[] data = tempStream.toByteArray();
//
//		if (data != null && data.length != 0) {
//			return new String(data, "utf-8");
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * Method to determine if a character is a whitespace.
//	 * 
//	 * @param c
//	 *            The character to check.
//	 * @return true if the character is a whitespace, false if not.
//	 */
//
//	private boolean isWhitespace(char c) {
//		if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
//			return true;
//
//		return false;
//	}
//
//	/**
//	 * Method to handle the attributes in a tag
//	 * 
//	 * @param data
//	 *            The section of the tag holding the attribute details
//	 */
//
//	private Hashtable handleAttributes(String data) {
//
//		Hashtable attributes = new Hashtable();
//		int len = data.length();
//		// int length = data.length();
//		int i = 0;
//		while (i < len) {
//			StringBuffer nameBuffer = new StringBuffer();
//
//			char thisChar = data.charAt(i);
//			while (isWhitespace(thisChar) && i < len) {
//				i++;
//				if (i == len)
//					break;
//				thisChar = data.charAt(i);
//			}
//			if (thisChar == '>' || i == len)
//				break;
//
//			while (thisChar != '=') {
//				nameBuffer.append(thisChar);
//
//				i++;
//				if (i == len)
//					break;
//
//				thisChar = data.charAt(i);
//			}
//
//			if (i == len)
//				break;
//
//			String name = nameBuffer.toString();
//
//			// See if first character is a character
//			i++;
//			thisChar = data.charAt(i);
//			while (isWhitespace(thisChar) && i < len) {
//				i++;
//				if (i == len)
//					break;
//				thisChar = data.charAt(i);
//			}
//
//			int breakOn = 0;
//			if (thisChar == '\"') {
//				breakOn = 1;
//			} else if (thisChar == '\'') {
//				breakOn = 2;
//			}
//
//			// Set up buffer for value parameter
//			StringBuffer valueBuffer = new StringBuffer();
//			if (breakOn == 0) {
//				valueBuffer.append(thisChar);
//			}
//
//			i++;
//			while (i < len) {
//				thisChar = data.charAt(i);
//				i++;
//				if (breakOn == 0 && isWhitespace(thisChar)) {
//					break;
//				} else if (breakOn == 1 && thisChar == '\"') {
//					break;
//				} else if (breakOn == 2 && thisChar == '\'') {
//					break;
//				}
//				valueBuffer.append(thisChar);
//			}
//			String value = valueBuffer.toString();
//			attributes.put(name, value);
//		}
//
//		return attributes;
//	}
//
//	/**
//	 * Method to handle the reading and dispatch of tag data.
//	 */
//
//	private boolean handleTag() throws Exception {
//		boolean startTag = true, emptyTag = false;
//		String tagName = null;
//		Hashtable attributes = null;
//
//		String data = readUntilEnd('>');
//
//		if (data == null) {
//			return false;
//			// throw new EndOfXMLException();
//		}
//		// System.out.println(">>>>:"+data);
//		if (data.startsWith("?"))
//			return true;
//
//		int substringStart = 0, substringEnd = data.length();
//
//		if (data.startsWith("/")) {
//			startTag = false;
//			substringStart++;
//		}
//
//		if (data.endsWith("/")) {
//			emptyTag = true;
//			substringEnd--;
//		}
//
//		data = data.substring(substringStart, substringEnd);
//		int spaceIdx = 0;
//		while (spaceIdx < data.length()
//				&& isWhitespace(data.charAt(spaceIdx)) == false)
//			spaceIdx++;
//
//		tagName = data.substring(0, spaceIdx);
//		if (convertTagsToLowerCase)
//			tagName = tagName.toLowerCase();
//
//		if (spaceIdx != data.length()) {
//			data = data.substring(spaceIdx + 1);
//			attributes = handleAttributes(data);
//		}
//
//		if (startTag) {
//			if (rootTag == null)
//				rootTag = tagName;
//			eventHandler.tagStarted(tagName, attributes);
//		}
//
//		if (emptyTag || !startTag) {
//			eventHandler.tagEnded(tagName);
//			// System.out.println("vvvvvvvvvv");
//			// if (rootTag != null && tagName.equals(rootTag))
//			// throw new EndOfXMLException();
//		}
//		return true;
//	}
//
//	/**
//	 * Method to handle the reading in and dispatching of events for plain text.
//	 */
//
//	private void handlePlainText() throws Exception {
//		// .out.println("xxxxxx");
//		String data = readUntilEnd('<');
//
//		if (data != null)
//			eventHandler.plaintextEncountered(data);
//	}

//	public void parse(byte[] data) throws Exception {
//		ByteArrayInputStream readStream = new ByteArrayInputStream(data);
//		parse(readStream);
//		readStream.close();
//		readStream = null;
//	}
//
//	/**
//	 * Parse wrapper for InputStreams
//	 * 
//	 * @param _inputReader
//	 *            The reader for the XML stream.
//	 */
//
//	public void parse(String str) throws Exception {
//		// is = _is;
//		// InputStreamReader isr = new InputStreamReader(is);
//		ByteArrayInputStream readStream = new ByteArrayInputStream(str
//				.getBytes("utf-8"));
//		parse(readStream);
//		readStream.close();
//		readStream = null;
//	}
//
//	/**
//	 * The main parsing loop.
//	 * 
//	 * @param _inputReader
//	 *            The reader for the XML stream.
//	 */
//
//	public void parse(InputStream _inputReader) throws Exception {
//		inputReader = _inputReader;
//		reader = new InputStreamReader(inputReader);
//		try {
//			initParser();
//			
//			while (true) {
//				handlePlainText();
//				if (!handleTag()) {
//					return;
//				}
//			}
//		} catch (EndOfXMLException x) {
//			// x.printStackTrace();
//			// The EndOfXMLException is purely used to drop out of the
//			// continuous loop.
//		}
//
//	}

//	private void initParser() throws IOException {
//		// TODO Auto-generated method stub
//		this.allowEntitiesInAttributes = true;
//
//		this.peek0 = reader.read();
//		this.peek1 = reader.read();
//
//		this.eof = this.peek0 == -1;
//
//
//		this.line = 1;
//		this.column = 1;
//	}
}
