package me.grax.jbytemod.res;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import me.grax.jbytemod.utils.ErrorDisplay;

public class LanguageRes {
  private final HashMap<String, String> map = new HashMap<>();

  public LanguageRes() {
    System.out.println("Reading Language XML..");
    this.readXML();
  }

  public String getResource(String desc) {
    return map.getOrDefault(desc, desc);
  }

  private void readXML() {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(getXML());
      doc.getDocumentElement().normalize();
      Element resources = doc.getDocumentElement();
      NodeList nodes = resources.getChildNodes();
      for(int i =0; i < nodes.getLength(); i++) {
        Node e = (Node) nodes.item(i);
        if(e.getNodeName().equals("string")) {
          Element el = (Element) e;
          map.put(el.getAttribute("name"), e.getTextContent());
        }
      }
      System.out.println("Successfully loaded " +  map.size() + " resources");
    } catch (Exception e) {
      new ErrorDisplay(e);
    }
  }

  private InputStream getXML() {
    InputStream is = LanguageRes.class.getResourceAsStream("locale/" + this.getLanguage() + ".xml");
    if (is == null) {
      System.out.println("Using default en.xml");
      is = LanguageRes.class.getResourceAsStream("locale/en.xml");
    }
    return is;
  }

  private String getLanguage() {
    return System.getProperty("user.language");
  }
}
