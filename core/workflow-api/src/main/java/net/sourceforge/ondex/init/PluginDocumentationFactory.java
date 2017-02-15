package net.sourceforge.ondex.init;

import net.sourceforge.ondex.annotations.*;

import java.util.regex.Pattern;

/**
 * @author hindlem, lysenko
 */
public class PluginDocumentationFactory {

    /**
     * @param pb
     * @return
     */
    public static String getDocumentation(PluginDescription pb) {
    	Class<?> plugin = PluginRegistry.getInstance().loadCls(pb.getCls());
    	
        StringBuffer sb = new StringBuffer();
        sb.append("<html> <body> <font  face=\"Calibri\">");
        sb.append("<h1>").append(pb.getName()).append("</h1>");

        DatabaseTarget target = plugin.getAnnotation(DatabaseTarget.class);
        if (target != null) {
            sb.append("<p>").append(target.description()).append("</p>");
        }

        Status status = plugin.getAnnotation(Status.class);
        if (status != null) {
            sb.append("<h2>Plugin status</h2>");
            sb.append("<p><b>").append(status.status()).append("</b>" + " ").append(status.description()).append("</p>");
        }

        Authors authors = plugin.getAnnotation(Authors.class);
        if (authors != null && authors.authors() != null && authors.authors().length != 0) {
            if (authors.authors().length > 1)
                sb.append("<h2>Authors</h2>");
            else
                sb.append("<h2>Author</h2>");
            sb.append("<ul>");
            for (int i = 0; i < authors.authors().length; i++) {
                sb.append("<li type=circle>");
                if (authors.emails() != null
                        && authors.emails().length >= (i + 1)
                        && authors.emails()[i] != null
                        && authors.emails()[i].length() != 0)
                    sb.append("<a href=\"mailto:").append(processEmail(authors.emails()[i])).append("\" subject=\"").append(pb.getName()).append("\">").append(authors.authors()[i]).append("</a>");
                else
                    sb.append(authors.authors()[i]);
                sb.append("</li>");
            }
            sb.append("</ul>");
        }

        Custodians custodians = plugin.getAnnotation(Custodians.class);
        if (custodians != null && custodians.custodians() != null && custodians.custodians().length != 0) {
            if (custodians.custodians().length > 1)
                sb.append("<h2>Custodians</h2>");
            else
                sb.append("<h2>Custodian</h2>");
            sb.append("<ul>");
            for (int i = 0; i < custodians.custodians().length; i++) {
                sb.append("<li type=circle>");
                if (custodians.emails() != null
                        && custodians.emails().length >= (i + 1)
                        && custodians.emails()[i] != null
                        && custodians.emails()[i].length() != 0)
                    sb.append("<a href=\"mailto:").append(processEmail(custodians.emails()[i])).append("\" subject=\"").append(pb.getName()).append("\">").append(custodians.custodians()[i]).append("</a>");
                else
                    sb.append(custodians.custodians()[i]);
                sb.append("</li>");
            }
            sb.append("</ul>");
        }

        if (target != null) {
            sb.append("<h2>Target versions of <a href=\"").append(target.url()).append("\">").append(target.name()).append("</a></h2>");
            sb.append("<ul>");
            for (String version : target.version()) {
                sb.append("<li type=circle>").append(version).append("</li>");
            }
            sb.append("</ul>");
        }
        if (pb.getDescription().trim().replaceAll("[^\\d||\\w]", "").length() == 0) {
            sb.append("<h2>Details</h2>");
            sb.append("<p>").append(pb.getDescription().trim()).append("</p>");
        }

        sb.append("</font> </body> </html>");
        return sb.toString();
    }

    private static Pattern emailPattern = Pattern.compile(" at ");

    private static String processEmail(String eMail) {
        return emailPattern.matcher(eMail).replaceAll("@");
    }

    public static String getArguments(PluginDescription pb) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html> <body> <font  face=\"Calibri\"> <table border=\"0\">" + " <tr>  <td COLSPAN=2>").append(pb.getDescription()).append("</td></tr>");
        sb.append(" <tr>  <td COLSPAN=2><B>Qualified class name of this Plugin:</B><br>").append(pb.getCls()).append("</td></tr>");
        for (ArgumentDescription ab : pb.getArgDef()) {
            String desc = ab.getDescription().trim();
            if (desc == null || desc.length() == 0) {
                desc = "No description";
            }
            sb.append("<tr> <td td width=\"30%\" valign=\"top\" align=\"LEFT\"><B>").append(ab.getName()).append("</B></td> <td>").append(desc).append("</td></tr>");
        }
        sb.append("</table> </font> </body> </html>");
        return sb.toString();
    }

    public static String getFiles(PluginDescription pb) {
        StringBuffer sb = new StringBuffer();
        Class<?> plugin = PluginRegistry.getInstance().loadCls(pb.getCls());

        sb.append("<html> <body> <font  face=\"Calibri\"> ");
        DataURL urlAnno = plugin.getAnnotation(DataURL.class);
        if (urlAnno != null) {
            sb.append("<h1>").append(urlAnno.name()).append("</h1>");
            sb.append("<p>").append(urlAnno.description()).append("</p>");
            for (String url : urlAnno.urls()) {
                sb.append("<li type=circle><a href=\"").append(url).append("\">").append(url).append("</a></li>");
            }
        }
        sb.append("</font> </body> </html>");

        return sb.toString();
    }

}
