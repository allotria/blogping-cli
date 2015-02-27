package com.atex.blogping.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.xml.sax.InputSource;

import javax.xml.xpath.*;
import java.io.StringReader;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class BlogpingClient {

    @Parameter(names = {"-h", "--host"}, description = "Hostname of the blogping server")
    private String host = "http://localhost:8080";

    @Parameter(names = {"-u", "--url"}, description = "URL to your blog")
    private String url;

    @Parameter(names = {"-n", "--name"}, description = "Name of your blog")
    private String name;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

    private static JCommander jCommander;


    public static void main(String[] args) throws Exception {
        BlogpingClient blogpingClient = new BlogpingClient();
        jCommander = new JCommander(blogpingClient, args);
        jCommander.setProgramName(BlogpingClient.class.getSimpleName());

        blogpingClient.executeCommand();
    }

    private void executeCommand() throws Exception {

        printHelpIfRequested();

        validateParameters();

        String response = callBlogpingService();

        printResponse(response);

    }

    private void printResponse(String response) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression messageXPath = xPath.compile("/response/message");
        String messageStr = (String) messageXPath.evaluate(getInputSourceFromString(response), XPathConstants.STRING);

        XPathExpression errorXPath = xPath.compile("/response/flerror");
        Double flerror = (Double) errorXPath.evaluate(getInputSourceFromString(response), XPathConstants.NUMBER);

        if (flerror > 0d) {
            System.err.println("ERROR: " + messageStr);
            System.exit(1);
        } else {
            System.out.println(messageStr);
            System.exit(0);
        }
    }

    private InputSource getInputSourceFromString(String response) {
        return new InputSource(new StringReader(response));
    }

    private String callBlogpingService() throws Exception {

        HttpClient client = new HttpClient();
        GetMethod getMethod = buildGetMethod();

        client.executeMethod(getMethod);

        return getMethod.getResponseBodyAsString();
    }

    private GetMethod buildGetMethod() throws URIException {
        final StringBuilder sb = new StringBuilder("http://localhost:8080");

        sb.append("/pingSiteForm");

        sb.append("?name=" + URIUtil.encodeWithinQuery(name));
        sb.append("&url=" + URIUtil.encodeWithinQuery(url));

        return new GetMethod(sb.toString());
    }

    private void printHelpIfRequested() {
        if (help) {
            jCommander.usage();
            System.exit(0);
        }
    }

    private void validateParameters() {
        if (isEmpty(url) || isEmpty(name)) {
            System.err.println("Missing mandatory parameter! See help for details.");
            jCommander.usage();
            System.exit(1);
        }

    }
}
