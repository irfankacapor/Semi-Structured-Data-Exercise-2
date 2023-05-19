package ssd;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TODO: Implement this content handler.
 */
public class ShipmentHandler extends DefaultHandler {
	/**
	 * Use this xPath variable to create xPath expression that can be
	 * evaluated over the XML document.
	 */
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	
	/**
	 * Store and manipulate the shipment XML document here.
	 */
	private Document shipmentDoc;
	
	/**
	 * This variable stores the text content of XML Elements.
	 */
	private String eleText;

	/**
	 * Insert local variables here
	 */

    
	
    public ShipmentHandler(Document doc) {
    	shipmentDoc = doc;
    }
    
    @Override
    /**
     * SAX calls this method to pass in character data
     */
  	public void characters(char[] text, int start, int length)
  			    throws SAXException {
  		eleText = new String(text, start, length);
  	}

    /**
     * 
     * Return the current stored shipment document
     * 
     * @return XML Document
     */
	public Document getDocument() {
		return shipmentDoc;
	}

	//***TODO***
//Specify additional methods to parse the freight document and modify the shipmentDoc

	// Write needed variables here
// Local variables for storing information about products
	private String producer = "";
	private String name = "";
	private String foodType = "";
	private String storageInfo = "";
	private String destination = "";
	private String sid = "";
	private String label = "";
	private List<String> tags = new ArrayList<>();
	private List<String> shortageCatalogs = new ArrayList<>();

	private String typeContent ="";
	private boolean inShortageElement = false;
	private List<String> freightTags = new ArrayList<>();

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if ("product".equals(localName)) {
			// Reset local variables when starting to parse a new product element
			producer = "";
			name = "";
			foodType = "";
			storageInfo = "";
			destination = "";
			sid = "";
			label = "";
			tags.clear();
		} else if ("type".equals(localName)) {
			// Extract foodType and storageInfo attributes from type element
			foodType = atts.getValue("foodType");
			storageInfo = atts.getValue("storageInfo");
		} else if ("ref".equals(localName)) {
			// Extract sid attribute from ref element
			sid = atts.getValue("sid");
		}else if ("shortage".equals(localName)) {
			// Set inShortageElement to true when starting to parse a shortage element
			inShortageElement = true;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if ("producer".equals(localName)) {
			producer = eleText;
		} else if ("name".equals(localName)) {
			name = eleText;
		} else if ("destination".equals(localName)) {
			destination = eleText;
		} else if ("label".equals(localName)) {
			label = eleText;
		} else if ("tag".equals(localName)) {
			tags.add(eleText);
			freightTags.add(eleText);
		} else if ("catalog".equals(localName) && inShortageElement) {
			// Add catalog element inside shortage element to shortageCatalogs list
			shortageCatalogs.add(eleText);
		} else if ("shortage".equals(localName)) {
			// Set inShortageElement to false when finished parsing a shortage element
			inShortageElement = false;
		} else if ("catalog".equals(localName) && "shortage".equals(qName)) {
			// Add catalog element inside shortage element to shortageCatalogs list
			shortageCatalogs.add(eleText);
		} else if ("type".equals(localName)) {
			// Store content of type element in freight.xml file
			typeContent = eleText.trim();
		} else if ("product".equals(localName)) {
			// Create a new product element in the shipment-sample-xsd-out.xml document
			Element productEle = shipmentDoc.createElement("product");

			// Set name element
			Element nameEle = shipmentDoc.createElement("name");
			nameEle.setTextContent(name);
			productEle.appendChild(nameEle);

			// Set type element
			Element typeEle = shipmentDoc.createElement("type");
			if ("food".equals(typeContent)) {
				Element foodEle = shipmentDoc.createElement("food");
				if (foodType != null) {
					foodEle.setAttribute("foodType", foodType);
				}
				if (storageInfo != null) {
					foodEle.setAttribute("storageInfo", storageInfo);
				}
				typeEle.appendChild(foodEle);
			} else if ("clothing".equals(typeContent)) {
				Element clothingEle = shipmentDoc.createElement("clothing");
				typeEle.appendChild(clothingEle);
			}
			productEle.appendChild(typeEle);

			// Set label element
			Element labelEle = shipmentDoc.createElement("label");

			// Create and append producer element
			Element producerEle = shipmentDoc.createElement("producer");
			producerEle.setTextContent(producer);
			labelEle.appendChild(producerEle);

			// Append text node
			labelEle.appendChild(shipmentDoc.createTextNode(" and will be shipped to the "));

			// Create and append destination element
			Element destinationEle = shipmentDoc.createElement("destination");
			destinationEle.setTextContent(destination);
			labelEle.appendChild(destinationEle);

			// Append text node
			labelEle.appendChild(shipmentDoc.createTextNode(". Currently "));

			// Create and append ref element
			Element refEle = shipmentDoc.createElement("ref");
			refEle.setAttribute("sid", sid);
			refEle.setTextContent("Ship " + sid);
			labelEle.appendChild(refEle);

			// Append text node
			labelEle.appendChild(shipmentDoc.createTextNode(" carries a batch of this product. " + label));

			productEle.appendChild(labelEle);

			// Set catalog element with random value
			Element catalogEle = shipmentDoc.createElement("catalog");
			catalogEle.setTextContent(String.format("E#%04d#aaa", new Random().nextInt(10000)));
			productEle.appendChild(catalogEle);

			// Set tags element
			System.out.println(tags);
			Element tagsEle = shipmentDoc.createElement("tags");
			for (String tag : tags) {
				Element tagEle = shipmentDoc.createElement("tag");
				tagEle.setTextContent(tag);
				tagsEle.appendChild(tagEle);
			}
			productEle.appendChild(tagsEle);

			// Append new product element to products element in shipment-sample-xsd-out.xml document
			NodeList productsList = shipmentDoc.getElementsByTagName("products");
			if (productsList.getLength() > 0) {
				Node productsNode = productsList.item(0);
				productsNode.appendChild(productEle);
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			// Remove all product elements from shipment-sample-xsd.xml that contain one of the catalog elements inside the shortage element in freight.xml file
			for (String catalog : shortageCatalogs) {
				XPathExpression xpe = xPath.compile("//products/product[catalog='" + catalog + "']");
				NodeList productList = (NodeList) xpe.evaluate(shipmentDoc, XPathConstants.NODESET);
				for (int i = 0; i < productList.getLength(); i++) {
					Node node = productList.item(i);
					node.getParentNode().removeChild(node);
				}
			}

			// Add new t elements for tags found in freight.xml file
			for (String tag : freightTags) {
				// Check if tag is already contained within the shipmentDoc tags element inside a t element
				XPathExpression xpe = xPath.compile("/shipment/tags/t[@tagname='" + tag + "']");
				NodeList tList = (NodeList) xpe.evaluate(shipmentDoc, XPathConstants.NODESET);
				if (tList.getLength() == 0) {
					// If tag is not already contained within the shipmentDoc tags element inside a t element, append a new t element to the tags containing this t element
					Element tEle = shipmentDoc.createElement("t");
					tEle.setAttribute("tagname", tag);
					xpe = xPath.compile("/shipment/tags");
					NodeList tagsList = (NodeList) xpe.evaluate(shipmentDoc, XPathConstants.NODESET);
					if (tagsList.getLength() > 0) {
						Node tagsNode = tagsList.item(0);
						tagsNode.appendChild(tEle);
					}
				}
			}
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}

