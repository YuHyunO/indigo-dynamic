package mb.dnm.core.dynamic.adaptersupport;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

@Slf4j
public class ClassPathFactory extends DefaultHandler {
    private String content;
    private LinkedList values;
    private boolean indicator;
    private ClassPathBean pathBean;
    String s = null;

    private ClassPathFactory(String filePath, String productName) {
        try {
            this.indicator = false;
            this.values = new LinkedList();
            this.pathBean = new ClassPathBean();
            this.content = this.readFile(filePath);
        } catch (IOException e) {
            log.error("", e);
        }

    }

    public static ClassPathFactory getInstance(String filePath, String productName) {
        return new ClassPathFactory(filePath, productName);
    }

    public ClassPathBean getBean() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.setErrorHandler(new DefaultHandler());
        xmlReader.parse(new InputSource(new ByteArrayInputStream(this.content.getBytes())));
        return this.pathBean;
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        try {
            if (qName.equals("product")) {
                this.pathBean.setName(attrs.getValue("name"));
                this.indicator = true;
            }

            if (qName.equals("classpathentry")) {
                this.pathBean.setPdName(attrs.getValue("pdName"));
                this.pathBean.setEntry(attrs.getValue("path"));
                this.indicator = true;
            }
        } catch (Exception e) {
            log.error("==============Exception : " + e.getMessage());
        }

    }

    public void characters(char[] ch, int start, int end) throws SAXException {
        this.s = new String(ch, start, end);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("product")) {
            this.indicator = false;
        }

        if (qName.equals("classpathentry")) {
            this.indicator = false;
        }

    }

    public void endDocument() throws SAXException {
    }

    public String readFile(String filePath) throws IOException {
        StringBuffer content = new StringBuffer();

        try {
            FileReader fis = new FileReader(filePath);
            BufferedReader bis = new BufferedReader(fis);
            String aLine = "";

            while((aLine = bis.readLine()) != null) {
                content.append(aLine + "\n");
            }
        } catch (Exception var6) {
            Exception e = var6;
            throw new IOException(e.getMessage());
        }

        return content.toString();
    }

    public static String replace(String s, String s1, String s2) {
        if (s != null && !s.equals("") && s1 != null && !s1.equals("") && s2 != null) {
            StringBuffer stringbuffer = new StringBuffer();
            int i = 0;
            int k = s1.length();

            try {
                int j;
                while((j = s.indexOf(s1, i)) >= 0) {
                    stringbuffer.append(s.substring(i, j));
                    stringbuffer.append(s2);
                    i = j + k;
                }

                stringbuffer.append(s.substring(i));
            } catch (Exception var7) {
                Exception e = var7;
                e.printStackTrace();
            }

            return stringbuffer.toString();
        } else {
            return s;
        }
    }

    public static String makeClassPath(String filePath, String productName) throws Exception {
        ClassPathFactory cpf = ClassPathFactory.getInstance(filePath, productName);
        ClassPathBean pathBean = cpf.getBean();
        String[] pdName = pathBean.getPdName();
        String[] entryList = pathBean.getEntry();
        int entrySize = pdName.length;
        String value = "";

        for(int i = 0; i < entrySize; ++i) {
            if (productName.equals(pdName[i])) {
                if (i == 0) {
                    value = entryList[0];
                } else {
                    value = value + File.pathSeparator + entryList[i];
                }
            }
        }

        return value;
    }

    public static List<URL> getUrls(String classpath) throws IOException{
        List<URL> urlList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while(tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken();
            try {
                urlList.add(new URL(repository));
            } catch (MalformedURLException e) {
                File file = new File(repository);
                URL url = new URL("file", null, file.getCanonicalPath());
                urlList.add(url);
            }
        }

        return urlList;
    }
}