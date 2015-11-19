
package hu.everit.utils.xsd.downloader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XsdDownloader
{

    public static class XsdNameSpaceContext implements NamespaceContext
    {

        private final Map<String, String> nameSpaceUrisByPrefixes;

        public XsdNameSpaceContext()
        {
            nameSpaceUrisByPrefixes = new HashMap<String, String>();
            nameSpaceUrisByPrefixes.put("xsd", "http://www.w3.org/2001/XMLSchema");
        }

        public String getNamespaceURI(final String prefix)
        {
            return nameSpaceUrisByPrefixes.get(prefix);
        }

        public String getPrefix(final String namespaceURI)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public Iterator getPrefixes(final String namespaceURI)
        {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * @param args
     */
    public static void main(final String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Only two parameters: 1--wsdl-url 2--xsds-prefix");
            return;
        }
        String xsdUrl = args[0];
        String filePrefix = args[1];
        XsdDownloader xsdDownloader = new XsdDownloader();
        xsdDownloader.setDownloadPrefix(filePrefix);
        try
        {
            xsdDownloader.downloadXsdRecurse(xsdUrl);
        }
        catch (TransformerConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TransformerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    Map<String, String> fileNamesByprocessedUrls = new HashMap<String, String>();

    private String downloadPrefix;

    private void downloadXsdRecurse(final String xsdUrl)
        throws IOException, ParserConfigurationException, SAXException, TransformerException
    {

        String outputFileName = downloadPrefix;
        if (fileNamesByprocessedUrls.size() > 0)
        {
            outputFileName = outputFileName + "-" + fileNamesByprocessedUrls.size();
        }
        outputFileName = outputFileName + ".xsd";
        fileNamesByprocessedUrls.put(xsdUrl, outputFileName);

		System.out.println("Download " + xsdUrl);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xsdUrl);

        processElementRecurse(doc.getDocumentElement());

        File outputFile = new File(outputFileName);
        TransformerFactory trf = TransformerFactory.newInstance();
        Transformer tr = trf.newTransformer();
        Source source = new DOMSource(doc);
        Result result = new StreamResult(outputFile);
        tr.transform(source, result);
    }

    private void processElementRecurse(final Element node)
        throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        NodeList nl = node.getChildNodes();
        for (int i = 0, n = nl.getLength(); i < n; i++)
        {
            Node childNode = nl.item(i);
            if (childNode instanceof Element)
            {
                Element childElement = (Element) childNode;
                if ("http://www.w3.org/2001/XMLSchema".equals(childElement.getNamespaceURI())
                    && (childElement.getLocalName().equals("import") || childElement.getLocalName().equals("include")))
                {
                    System.out.println("New Element Found");
                    String schLoc = childElement.getAttribute("schemaLocation");

                    if (!fileNamesByprocessedUrls.containsKey(schLoc))
                    {
                        downloadXsdRecurse(schLoc);
                        String newLoc = fileNamesByprocessedUrls.get(schLoc);
                        if (newLoc != null)
                        {
                            childElement.setAttribute("schemaLocation", newLoc);
                        }
                    }
                    else
                    {
                        String newLoc = fileNamesByprocessedUrls.get(schLoc);
                        childElement.setAttribute("schemaLocation", newLoc);
                    }
                }
                else if ("http://schemas.xmlsoap.org/wsdl/".equals(childElement.getNamespaceURI())
                         && (childElement.getLocalName().equals("import") || childElement.getLocalName().equals("include")))
                {
                    System.out.println("WSDL Found");
                    String schLoc = childElement.getAttribute("location");

                    if (!fileNamesByprocessedUrls.containsKey(schLoc))
                    {
                        downloadXsdRecurse(schLoc);
                        String newLoc = fileNamesByprocessedUrls.get(schLoc);
                        if (newLoc != null)
                        {
                            childElement.setAttribute("location", newLoc);
                        }
                    }
                    else
                    {
                        String newLoc = fileNamesByprocessedUrls.get(schLoc);
                        childElement.setAttribute("location", newLoc);
                    }
                }
                else
                {
                    processElementRecurse(childElement);
                }
            }
        }
    }

    public void setDownloadPrefix(final String downloadPrefix)
    {
        this.downloadPrefix = downloadPrefix;
    }

}
