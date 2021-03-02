package com.buildingsmart.tech.ifcowl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PSetReader {
	
	
	Map<String,List<PSet>> psetmap;
	Map<String,PEnum> penummap;
	
	public PSetReader() {
		this.psetmap=new HashMap<String,List<PSet>>();
		this.penummap=new HashMap<String,PEnum>();
	}
	
	
	public void parsePSetList(String string) throws ParserConfigurationException, SAXException, IOException {
		File[] files=new File(string).listFiles();
		for (File file:files) {
			PSet pset=parsePSet(file);
			List<String> applicableClasses=pset.getApplicableEntities();
			for (String key: applicableClasses) {
				if (psetmap.get(key)==null) {
					ArrayList<PSet> psets=new ArrayList<PSet>();
					psets.add(pset);
					psetmap.put(key, psets);
				}else {
					psetmap.get(key).add(pset);
				}
			}
		}
	}
	
	public PSet parsePSet(File file) throws ParserConfigurationException, SAXException, IOException {
		PSet pset=new PSet();
		pset.setApplicableEntities(new ArrayList<String>());
		pset.setProperties(new ArrayList<Property>());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		pset.setName(root.getElementsByTagName("Name").item(0).getTextContent());
//		pset.setDescription(root.getElementsByTagName("Definition").item(0).getTextContent());
		NodeList nList = root.getElementsByTagName("ApplicableClasses");
	
		 
		for (int temp = 0; temp < nList.getLength(); temp++)
		{
		 Node node = nList.item(temp);
		 if (node.getNodeType() == Node.ELEMENT_NODE)
		 {
		    //Print each employee's detail
		    Element eElement = (Element) node;
		    NodeList classNames=eElement.getElementsByTagName("ClassName");
		    for (int i = 0; i < classNames.getLength(); i++)
			{
			 Node className = classNames.item(i);
			 if (className.getNodeType() == Node.ELEMENT_NODE)
			 {Element classNameE=(Element)className;

			 pset.getApplicableEntities().add(classNameE.getTextContent());
			 
			 }
			 }
		 }
		}
		
		NodeList propertyList = document.getElementsByTagName("PropertyDef");
		for (int j=0;j<propertyList.getLength();j++) {
			Element prop=(Element)propertyList.item(j);
			Property property=parseProperty(prop);
			pset.getProperties().add(property);
		}
		

		return pset;
		
	}
	
	public Property parseProperty(Element prop) {
		Property property=new Property();
		String name=prop.getElementsByTagName("Name").item(0).getTextContent();
//		String description=prop.getElementsByTagName("Definition").item(0).getTextContent();
		property.setName(name);
//		property.setDescription(description);
		
		Element propertyType=(Element)prop.getElementsByTagName("PropertyType").item(0);
		NodeList nList = propertyType.getChildNodes();
		
		 
		for (int temp = 0; temp < nList.getLength(); temp++)
		{
		 Node node = nList.item(temp);
		 if (node.getNodeType() == Node.ELEMENT_NODE)
		 {
		    //Print each employee's detail
		    Element type = (Element) node;
		String datatype=new String();
		String pt=type.getNodeName();
		if (pt.equals("TypePropertySingleValue")) {
			pt="P_SINGLEVALUE";
			datatype=((Element)(type.getElementsByTagName("DataType").item(0))).getAttribute("type");
		}else if (pt.equals("TypePropertyBoundedValue")) {
			pt="P_BOUNDEDVALUE";
			datatype=((Element)(type.getElementsByTagName("DataType").item(0))).getAttribute("type");
		}else if (pt.equals("TypePropertyListValue")) {
			pt="P_LISTVALUE";
			datatype=((Element)(type.getElementsByTagName("DataType").item(0))).getAttribute("type");
		}else if (pt.equals("TypePropertyTableValue")) {
			pt="P_TABLEVALUE";
			Element definingValue=((Element)(type.getElementsByTagName("DefiningValue").item(0)));
			Element definedValue=((Element)(type.getElementsByTagName("DefinedValue").item(0)));
			datatype=((Element)(definingValue.getElementsByTagName("DataType").item(0))).getAttribute("type");
			datatype=datatype+"/"+((Element)(definedValue.getElementsByTagName("DataType").item(0))).getAttribute("type");
		}else if(pt.equals("TypePropertyReferenceValue")) {
			pt="P_REFERENCEVALUE";
			datatype=type.getAttribute("reftype");
		}else if(pt.equals("TypePropertyEnumeratedValue")) {
			pt="P_ENUMERATEDVALUE";
		Element enumlist=(Element)(type.getElementsByTagName("EnumList").item(0));
		NodeList enumitems=enumlist.getElementsByTagName("EnumItem");
		PEnum penum=new PEnum();
		datatype=enumlist.getAttribute("name");
		penum.setName(datatype);
		penum.setItems(new ArrayList<String>());
		for (int i=0;i<enumitems.getLength();i++) {
			penum.getItems().add(((Element)enumitems.item(i)).getTextContent());
		}
		property.setPenum(penum);
		penummap.put(penum.getName().toUpperCase(), penum);
		 }
		

		property.setDatatype(datatype);
		property.setType(pt);
		 }}	
		return property;
	}

}
