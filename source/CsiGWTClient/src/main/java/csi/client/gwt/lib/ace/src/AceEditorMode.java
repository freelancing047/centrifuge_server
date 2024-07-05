// Copyright (c) 2011 David H. Hovemeyer <david.hovemeyer@gmail.com>
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package csi.client.gwt.lib.ace.src;

/**
 * Enumeration for ACE editor modes.
 * Note that the corresponding .js file must be loaded
 * before a mode can be set.
 */
public enum AceEditorMode {
	/** ABAP (Advanced Business Application Programming). */
	ABAP("abap"), //$NON-NLS-1$
	/** Actionscript. */
	ACTIONSCRIPT("actionscript"), //$NON-NLS-1$
	/** Ada. */
	ADA("ada"), //$NON-NLS-1$
	/** ASCIIDOC. */
	ASCIIDOC("asciidoc"), //$NON-NLS-1$
	/** Assembly (x86). */
	ASSEMBLY_X86("assembly_x86"), //$NON-NLS-1$
	/** Auto Hotkey. */
	AUTOHOTKEY("autohotkey"), //$NON-NLS-1$
	/** Batch file. */
	BATCHFILE("batchfile"), //$NON-NLS-1$
	/** c9search */
	C9SEARCH("c9search"), //$NON-NLS-1$
	/** C/C++. */
	C_CPP("c_cpp"), //$NON-NLS-1$
	/** Clojure. */
	CLOJURE("clojure"), //$NON-NLS-1$
	/** COBOL. */
	COBOL("cobol"), //$NON-NLS-1$
	/** Coffee. */
	COFFEE("coffee"), //$NON-NLS-1$
	/** ColdFusion. */
	COLDFUSION("coldfusion"), //$NON-NLS-1$
	/** C#. */
	CSHARP("csharp"), //$NON-NLS-1$
	/** CSS. */
	CSS("css"), //$NON-NLS-1$
	/* Curly. */
	CURLY("curly"), //$NON-NLS-1$
	/** Dart. */
	DART("Dart"), //$NON-NLS-1$
	/** Diff. */
	DIFF("diff"), //$NON-NLS-1$
	/** Django. */
	DJANGO("django"), //$NON-NLS-1$
	/** D. */
	D("d"), //$NON-NLS-1$
	/** Dot. */
	DOT("dot"), //$NON-NLS-1$
	/** EJS (Embedded Javascript). */
	EJS("ejs"), //$NON-NLS-1$
	/** Erlang. */
	ERLANG("erlang"), //$NON-NLS-1$
	/** Forth. */
	FORTH("forth"), //$NON-NLS-1$
	/** FTL. */
	FTL("ftl"), //$NON-NLS-1$
	/** GLSL (OpenGL Shading Language). */
	GLSL("glsl"), //$NON-NLS-1$
	/** Go (http://golang.org/). */
	GOLANG("golang"), //$NON-NLS-1$
	/** Groovy. */
	GROOVY("groovy"), //$NON-NLS-1$
	/** HAML. */
	HAML("haml"), //$NON-NLS-1$
	/** Haskell. */
	HASKELL("haskell"), //$NON-NLS-1$
	/** Haxe. */
	HAXE("haxe"), //$NON-NLS-1$
	/** HTML. */
	HTML("html"), //$NON-NLS-1$
	/** HTML (Ruby). */
	HTML_RUBY("html_ruby"), //$NON-NLS-1$
	/** Ini file. */
	INI("ini"), //$NON-NLS-1$
	/** JADE. */
	JADE("jade"), //$NON-NLS-1$
	/** JAVA. */
	JAVA("java"), //$NON-NLS-1$
	/** Javascript. */
	JAVASCRIPT("javascript"), //$NON-NLS-1$
	/** JSONiq, the JSON Query Language. */
	JSONIQ("jsoniq"), //$NON-NLS-1$
	/** JSON. */
	JSON("json"), //$NON-NLS-1$
	/** JSP, Java Server Pages. */
	JSP("jsp"), //$NON-NLS-1$
	/** JSX. */
	JSX("jsx"), //$NON-NLS-1$
	/** Julia. */
	JULIA("julia"), //$NON-NLS-1$
	/** LaTeX. */
	LATEX("latex"), //$NON-NLS-1$
	/** Less. */
	LESS("less"), //$NON-NLS-1$
	/** Liquid. */
	LIQUID("liquid"), //$NON-NLS-1$
	/** LISP. */
	LISP("lisp"), //$NON-NLS-1$
	/** Livescript. */
	LIVESCRIPT("livescript"), //$NON-NLS-1$
	/** LogiQL. */
	LOGIQL("logiql"), //$NON-NLS-1$
	/** LSL. */
	LSL("lsl"), //$NON-NLS-1$
	/** Lua. */
	LUA("lua"), //$NON-NLS-1$
	/** Luapage. */
	LUAPAGE("luapage"), //$NON-NLS-1$
	/** Lucene. */
	LUCENE("lucene"), //$NON-NLS-1$
	/** Makefile. */
	MAKEFILE("makefile"), //$NON-NLS-1$
	/** Markdown. */
	MARKDOWN("markdown"), //$NON-NLS-1$
	/** Matlab. */
	MATLAB("matlab"), //$NON-NLS-1$
	/** MUSHCode (High Rules). */
	MUSHCODE_HIGH_RULES("mushcode_high_rules"), //$NON-NLS-1$
	/** MUSHCode. */
	MUSHCODE("mushcode"), //$NON-NLS-1$
	/** MySQL. */
	MYSQL("mysql"), //$NON-NLS-1$
	/** Objective C. */
	OBJECTIVEC("objectivec"), //$NON-NLS-1$
	/** OCaml. */
	OCAML("ocaml"), //$NON-NLS-1$
	/** Pascal. */
	PASCAL("pascal"), //$NON-NLS-1$
	/** Perl. */
	PERL("perl"), //$NON-NLS-1$
	/** PgSQL. */
	PGSQL("pgsql"), //$NON-NLS-1$
	/** PHP. */
	PHP("php"), //$NON-NLS-1$
	/** PowerShell. */
	POWERSHELL("powershell"), //$NON-NLS-1$
	/** Prolog. */
	PROLOG("prolog"), //$NON-NLS-1$
	/** Java properties file. */
	PROPERTIES("properties"), //$NON-NLS-1$
	/** Python. */
	PYTHON("python"), //$NON-NLS-1$
	/** RDoc (Ruby documentation). */
	RDOC("rdoc"), //$NON-NLS-1$
	/** RHTML. */
	RHTML("rhtml"), //$NON-NLS-1$
	/** R. */
	R("r"), //$NON-NLS-1$
	/** Ruby. */
	RUBY("ruby"), //$NON-NLS-1$
	/** Rust. */
	RUST("rust"), //$NON-NLS-1$
	/** SASS. */
	SASS("sass"), //$NON-NLS-1$
	/** Scad. */
	SCAD("scad"), //$NON-NLS-1$
	/** Scala. */
	SCALA("scala"), //$NON-NLS-1$
	/** Scheme. */
	SCHEME("scheme"), //$NON-NLS-1$
	/** SCSS. */
	SCSS("scss"), //$NON-NLS-1$
	/** Sh (Bourne shell). */
	SH("sh"), //$NON-NLS-1$
	/** Snippets. */
	SNIPPETS("snippets"), //$NON-NLS-1$
	/** SQL. */
	SQL("sql"), //$NON-NLS-1$
	/** Stylus. */
	STYLUS("stylus"), //$NON-NLS-1$
	/** SVG. */
	SVG("svg"), //$NON-NLS-1$
	/** Tcl. */
	TCL("tcl"), //$NON-NLS-1$
	/** TeX. */
	TEX("tex"), //$NON-NLS-1$
	/** Text. */
	TEXT("text"), //$NON-NLS-1$
	/** Textile. */
	TEXTILE("textile"), //$NON-NLS-1$
	/** TOML. */
	TOML("toml"), //$NON-NLS-1$
	/** TWIG. */
	TWIG("twig"), //$NON-NLS-1$
	/** TypeScript. */
	TYPESCRIPT("typescript"), //$NON-NLS-1$
	/** VBScript. */
	VBSCRIPT("vbscript"), //$NON-NLS-1$
	/** Velocity. */
	VELOCITY("velocity"), //$NON-NLS-1$
	/** Verilog. */
	VERILOG("verilog"), //$NON-NLS-1$
	/** XML. */
	XML("xml"), //$NON-NLS-1$
	/** XQuery. */
	XQUERY("xquery"), //$NON-NLS-1$
	/** YAML. */
	YAML("yaml"); //$NON-NLS-1$
	
	private final String name;
	
	private AceEditorMode(String name) {
		this.name = name;
	}
	
	/**
	 * @return mode name (e.g., "java" for Java mode)
	 */
	public String getName() {
		return name;
	}
}
