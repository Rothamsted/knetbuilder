package net.sourceforge.ondex.scripting.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Static methods to write out scripting reference information into html.
 * Start and end methods must be called to initiate and complete the process.
 * The rest is obvious from method names.
 *
 * @author lysenkoa
 */
public class AutoDoc {
    private static StringBuffer html = null;

    private AutoDoc() {
    }

    public static void begin() {
        html = new StringBuffer();
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
                "<HTML>\n" +
                "<HEAD>\n" +
                "<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=windows-1252\">\n" +
                "<TITLE></TITLE>\n" +
                "<META NAME=\"GENERATOR\" CONTENT=\"OpenOffice.org 2.3  (Win32)\">\n" +
                "<META NAME=\"AUTHOR\" CONTENT=\"a Z\">\n" +
                "<META NAME=\"CREATED\" CONTENT=\"20080216;18360837\">\n" +
                "<META NAME=\"CHANGEDBY\" CONTENT=\"a Z\">\n" +
                "<META NAME=\"CHANGED\" CONTENT=\"20080216;18574537\">\n" +
                "<STYLE TYPE=\"text/css\">\n" +
                "<!--\n" +
                "@page { size: 21cm 29.7cm; margin: 2cm }\n" +
                "P { margin-bottom: 0.21cm }\n" +
                "TD P { margin-bottom: 0cm }\n" +
                "-->\n" +
                "</STYLE>\n" +
                "</HEAD>\n" +
                "<BODY LANG=\"en-GB\" DIR=\"LTR\">\n" +
                "<P ALIGN=JUSTIFY><FONT FACE=\"Calibri\"><FONT SIZE=6 STYLE=\"font-size: 26pt\"><B>ONDEX/OVTK\n" +
                "scripting reference</B></FONT></FONT></P>\n" +
                "<body lang=EN-GB link=blue vlink=purple>\n" +
                "<div class=Section1>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>Author: Artem Lysenko, artem.lysenko_at_rothamsted.ac.uk</p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>Ondex scripting\n" +
                "engine allows flexible manipulation of graphs and annotation at run-time and is\n" +
                "implemented in JavaScript. More information about Javascript syntax and data\n" +
                "types can be found on the following sites:</span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'><a\n" +
                "href=\"http://en.wikipedia.org/wiki/JavaScript\">http://en.wikipedia.org/wiki/JavaScript</a></span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'><a\n" +
                "href=\"http://en.wikipedia.org/wiki/JavaScript_syntax\">http://en.wikipedia.org/wiki/JavaScript_syntax</a></span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'><a\n" +
                "href=\"http://www.crockford.com/javascript/javascript.html\">http://www.crockford.com/javascript/javascript.html</a></span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>A useful guide\n" +
                "about the differences between JavaScript and Java:</span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'><a\n" +
                "href=\"http://peter.michaux.ca/article/5004\">http://peter.michaux.ca/article/5004</a></span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>It is\n" +
                "backed by the Mozilla Rhino scripting engine that allows native Java classes to\n" +
                "be used alongside JavaSctript objects. For more information about Rhino-specific\n" +
                "features and how to use them please refer to their website: <a\n" +
                "href=\"http://www.mozilla.org/rhino/\">http://www.mozilla.org/rhino/</a>.</span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>All of\n" +
                "the necessary functions and objects needed to access data elements, graph and attributes\n" +
                "of concepts and relations can be accessed via scripting interface. In OVTK all commands\n" +
                "work on the graph associated with the currently selected viewer.</span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>At the\n" +
                "moment all of the ONDEX data types are wrapped in a standardised way all of\n" +
                "the getters with no argument and setter that take one argument become properties\n" +
                "in JavaScript representation and can be used with assignment operator '=' to\n" +
                "write and read from them. All other methods become functions and are used with\n" +
                "a standard Java notation e.g. fn(arg0, arg1, arg2). There are also convenience\n" +
                "methods added that allow terms of controlled vocabulary, like accessions,\n" +
                "attribute names, etc. to be supplied as a string as well as an original data\n" +
                "type. For example to get concept Attribute: concept</span><span style='font-size:\n" +
                "11.0pt;font-family:Calibri;color:black'>.getConceptGDS</span><span\n" +
                "style='font-size:11.0pt;font-family:Calibri;color:black'>(\"TAXID\"</span><span\n" +
                "style='font-size:11.0pt;font-family:Calibri'>). Internally the strings are resolved\n" +
                "against the metadata as per norm. When testing new Java code in OVTK, it can be\n" +
                "made into a new global function by adding it as a static method to <i>net.sourceforge.ondex.ovtk2.ui.scripting.OVTK2CustomFunctions\n" +
                "</i>class.</span></p>\n" +
                "<p class=MsoNormal><span style='font-size:11.0pt;font-family:Calibri'>Here is\n" +
                "the list of all currently supported options and how they can be called from the\n" +
                "scripting interface. For the full documentation of what particular methods do\n" +
                "please refer to the ONDEX/OVTK JavaDoc and look for the original methods/classes\n" +
                "that back the JavaScript representation. If you add new features and need to\n" +
                "regenerate the documentation, delete 'Scripting_ref.htm' (this file) to have it\n" +
                "regenerated with the new information included.</span></p>\n" +
                "</div>" +
                "<TABLE WIDTH=90% BORDER=0 CELLPADDING=5 CELLSPACING=0>\n" +
                "<COL WIDTH=20*>\n" +
                "<COL WIDTH=20*>\n" +
                "<COL WIDTH=20*>\n" +
                "<COL WIDTH=70*>");
    }

    public static void addMajorHeader(String content) {
        html.append("<TR>\n" +
                "<TD COLSPAN=4 WIDTH=100% VALIGN=TOP>\n" +
                "<a name='"+content+"'>" +
                "<P><FONT FACE=\"Calibri\"><FONT SIZE=6 STYLE=\"font-size: 22pt\"><B>\n" + content + "</B></FONT></FONT></P>\n" +
                "</a>" +
                "</TD>\n" +
                "</TR>");
    }

    public static void addMajorSubitemType1(String info1, String info2) {
        html.append("<TR VALIGN=BOTTOM>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=3 WIDTH=90%>\n" +
                "<a name='"+info1+"'>" +
                "<P><FONT FACE=\"Calibri\"><FONT SIZE=5><B>\n" + info1 + "</B></FONT></FONT><BR/>\n" +
                "<FONT FACE=\"Calibri\">\n" + info2 + "</FONT></P>\n" +
                "</a>" +
                "</TD>\n" +
                "</TR>");
    }

    public static void addMinorHeader(String info) {
        html.append("<TR VALIGN=BOTTOM>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=2 WIDTH=80%>\n" +
                "<P><FONT FACE=\"Calibri\"><FONT SIZE=4 STYLE=\"font-size: 16pt\"><I><B>\n" + info + "</B></I></FONT></FONT></P>\n" +
                "</TD>\n" +
                "</TR>");
    }

    public static void addMinorSubitem(String info1, String info2) {
        html.append("<TR VALIGN=MIDDLE>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=1 WIDTH=10%>\n" +
                "<P><BR>\n" +
                "</P>\n" +
                "</TD>\n" +
                "<TD COLSPAN=1 WIDTH=70%>\n" +
                "<P><FONT FACE=\"Calibri\"><FONT SIZE=3><B>\n" + info1 +
                "</B></FONT></FONT><BR/>\n" +
                "<FONT FACE=\"Calibri\">\n" + info2 + "</FONT></P>\n" +
                "</TD>\n" +
                "</TR>");
    }

    public static void end() {
        html.append("</TABLE>\n" +
                "<P STYLE=\"margin-bottom: 0cm\"><BR>\n" +
                "</P>\n" +
                "</BODY>\n" +
                "</HTML>");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("Scripting_ref.htm"));
            out.append(html);
            out.close();
        } catch (Exception e) {
        }
        finally {
            html = null;
        }
    }

	public static void addSectionLink(String simpleName) {
		html.append("<FONT FACE=\"Calibri\"><FONT SIZE=3><I><B>\n" +
				"<a href='#"+simpleName+"'>"+simpleName+"</a>" +
                "</B></I></FONT></FONT><BR/>\n");
	}
}