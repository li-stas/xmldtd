package com.lista;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Hello world!
 * https://vike.io/ru/557148/ - Как проверить XML с Dtd с помощью Java?
 */
public class App {
    public static void main(String[] args)  {
        String cFileDtd = "group.dtd";
        String cFileSourece;
        String cFileTarget;
        if (args.length < 2) {
            cFileSourece = "stud.xml";
            cFileTarget = "nestud.xml";
        } else {
            cFileSourece = args[0];
            cFileTarget = args[1];
        }
        //делать проверку на соответвие
        Document doc = getXmlDoc(cFileSourece);
        if (doc != null) {
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("student"); // сколько блоков Студент в файле
            System.out.println("----------------------------");
            for (int student = 0; student < nList.getLength(); student++) {

                Node nNode = nList.item(student);
                System.out.println("nCurrent Element :" + nNode.getNodeName());

                // чтение <student firstname="St_FM1" lastname="St_LM1" groupnumber="1">
                Element eElement = (Element) nNode;
                System.out.println("student : " + eElement.getAttribute("firstname")
                        +" "+eElement.getAttribute("lastname")+" "+eElement.getAttribute("groupnumber"));

                // сколько внутри узлов subject
                int cntNodeSubject = eElement.getElementsByTagName("subject").getLength();
                System.out.printf("  cntNodeSubject : %d\n", cntNodeSubject);
                double nSumMark = 0;
                for (int subject = 0; subject < cntNodeSubject; subject++) {
                    Element eSubject = (Element) eElement.getElementsByTagName("subject").item(subject);
                    String cMarkValue = eSubject.getAttribute("mark");
                    System.out.printf("  subject %d: title=%s, mark=%s\n", subject, eSubject.getAttribute("title"), cMarkValue);
                    nSumMark += Double.parseDouble(cMarkValue);
                }

                // чтение такого значения <average>5</average>
                String cOldAverage = eElement.getElementsByTagName("average").item(0).getTextContent();
                System.out.println("OLD average : " + cOldAverage);

                double oldAerage = Double.parseDouble(cOldAverage);
                double newAverage = new BigDecimal(nSumMark / cntNodeSubject).setScale(1, RoundingMode.HALF_UP).doubleValue();

                if (oldAerage - newAverage !=0) {
                    System.out.printf("average : sum=%f, cnt=%d, avg=%.1f\n", nSumMark, cntNodeSubject, newAverage);
                    // запись
                    eElement.getElementsByTagName("average").item(0).setTextContent(String.format("%.1f", newAverage));
                    System.out.println("NEW average : " + eElement.getElementsByTagName("average").item(0).getTextContent());
                }
            }
            writeDocument(doc, cFileTarget);
            System.out.printf("\ncFileTarget : %s\n", cFileTarget);
        }

    }

    private static Document getXmlDoc(String cFile)  {
        Document doc = null;
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    // do something more useful in each of these handlers
                    //System.out.println("warning " + e.getMessage());
                    //exception.printStackTrace();
                    throw new SAXException("warning document builder", e);
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    // do something more useful in each of these handlers
                    //System.out.println("error " + e.getMessage());
                    //e.printStackTrace();
                    throw new SAXException("error builder " + e.getMessage(), e);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    // do something more useful in each of these handlers
                    //System.out.println("fatalError " + e.getMessage());
                    throw new SAXException("fatalError document builder", e);
                }

            });
            File fXmlFile = new File(cFile);

            doc = builder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

        } catch (SAXException e) {
            System.out.println( e.getMessage());
            //e.printStackTrace();
        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
        //ParserConfigurationException
        return doc;
    }

    /**
     *  // Функция для сохранения DOM в файл
     * https://java-course.ru/begin/xml/
     * @param document
     * @throws TransformerFactoryConfigurationError
     */
    private static void writeDocument(Document document, String cFile) throws TransformerFactoryConfigurationError {
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(cFile);
            StreamResult result = new StreamResult(fos);
            tr.transform(source, result);
        } catch (IOException | TransformerException e) {
            e.printStackTrace(System.out);
        }
    }
}
