package func;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.*;

import java.net.URI;
import java.util.List;

import com.scroogexhtml.ScroogeXHTML;
import com.scroogexhtml.css.LengthUnit;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Java class implements a Google Cloud Function (HttpFunction) that converts an RTF document 
 * (from a specific URL) to HTML using the ScroogeXHTML library. 
 * The function reads an RTF file from a query parameter URL, converts it, and responds with the 
 * HTML or usage instructions.
 */
public class HelloWorld implements HttpFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorld.class);

    static final String DEFAULT_CSS = """
            body,p {
              margin: 0;
            }
            td {
              vertical-align: top;
              border: 1px solid #D3D3D3;
            }
            table {
              border-collapse: collapse;
            }""";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {

        if (request.getQueryParameters() == null || request.getQueryParameters().get("url") == null) {
            // Display usage instructions if the URL parameter is missing
            response.setStatusCode(400); // Bad Request
            BufferedWriter writer = response.getWriter();
            writer.write("<html><body>");
            writer.write("<h1>RTF to HTML Converter</h1>");
            writer.write("<p>Please provide a 'url' parameter pointing to an RTF file.</p>");
            writer.write("<p>Example usage: <code>?url=https://scroogexhtml.com/rtf/features-fonts.rtf</code></p>");
            writer.write("</body></html>");
            return;
        }

        List<String> qp = request.getQueryParameters().get("url");
        String url = qp.getFirst();

        if (url.startsWith("https://scroogexhtml.com/rtf/")) {

            // Log the URL being processed
            LOGGER.info("Converting URL to InputStream: {}", url);

            try {
                ByteArrayInputStream rtfInputStream = getCurrentRtf(url);
                if (rtfInputStream != null) {
                    ScroogeXHTML s = createScrooge();
                    String html = s.convert(rtfInputStream);
                    BufferedWriter writer = response.getWriter();
                    writer.write(html);
                } else {
                    // Display usage instructions if the input stream is null
                    response.setStatusCode(500);
                    BufferedWriter writer = response.getWriter();
                    writer.write("<html><body>");
                    writer.write("<h1>RTF to HTML Converter</h1>");
                    writer.write("<p>Please provide a 'url' parameter pointing to an RTF file.</p>");
                    writer.write("<p>Example usage: <code>?url=https://scroogexhtml.com/rtf/features-fonts.rtf</code></p>");
                    writer.write("</body></html>");
                }
            } catch (Exception e) {
                response.setStatusCode(500);
                BufferedWriter writer = response.getWriter();
                writer.write("<html><body>");
                writer.write("<h1>Error</h1>");
                writer.write("<p>Failed to process the RTF document: " + e.getMessage() + "</p>");
                writer.write("</body></html>");
            }
        } else {
            // Display usage instructions if the URL parameter is invalid
            response.setStatusCode(400); // Bad Request
            BufferedWriter writer = response.getWriter();
            writer.write("<html><body>");
            writer.write("<h1>RTF to HTML Converter</h1>");
            writer.write("<p>Please provide a 'url' parameter pointing to an RTF file.</p>");
            writer.write("<p>Example usage: <code>?url=https://scroogexhtml.com/rtf/features-fonts.rtf</code></p>");
            writer.write("</body></html>");
        }
    }

    ScroogeXHTML createScrooge() {

        ScroogeXHTML scrooge = new ScroogeXHTML();
        scrooge.setAddOuterHTML(true);

        // Head options
        scrooge.getHtmlHeadConfig().setMetaAuthor("https://www.scroogexhtml.com/");
        scrooge.getHtmlHeadConfig().setStyleSheetInclude(DEFAULT_CSS);
        scrooge.getHtmlHeadConfig().setIncludeDefaultFontStyle(true);
        scrooge.setDefaultLanguage("en");

        // Font (character) Formatting Properties
        scrooge.getCharPropConvConfig().setConvertLanguage(true);
        scrooge.getCharPropConvConfig().setFontSizeUnit(LengthUnit.POINT); // default is em

        // Paragraphs
        scrooge.getParaPropConvConfig().setConvertIndent(true);
        scrooge.getParaPropConvConfig().setConvertParagraphBorders(true);

        // Special options
        scrooge.setConvertBookmarks(true);
        scrooge.setConvertEmptyParagraphs(true);
        scrooge.setConvertFootnotes(true);
        scrooge.setConvertHyperlinks(true);
        scrooge.setConvertPictures(true);
        scrooge.setConvertTables(true);

        // experimental Table options
        // scrooge.getTablePropConvConfig().setConvertCellStyles(true);
        // scrooge.getTablePropConvConfig().setConvertNestedTables(true);
        //scrooge.getTablePropConvConfig().setConvertWidthToPercent(true);

        return scrooge;
    }

    ByteArrayInputStream getCurrentRtf(String url) {
        byte[] fileContent;
        try {
            fileContent = IOUtils.toByteArray(URI.create(url));
            return new ByteArrayInputStream(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}