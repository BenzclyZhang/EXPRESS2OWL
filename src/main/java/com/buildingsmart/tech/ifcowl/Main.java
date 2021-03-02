package com.buildingsmart.tech.ifcowl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.xml.sax.SAXException;

import com.buildingsmart.tech.ifcowl.vo.AttributeVO;
import com.buildingsmart.tech.ifcowl.vo.EntityVO;
import com.buildingsmart.tech.ifcowl.vo.TypeVO;

import fi.ni.rdf.Namespace;

public class Main {

	String in;
	ExpressReader er;
	PSetReader pr;
	PackageReader par;
	HSSFWorkbook workbook;
	Map<String,TypeVO> predefinedTypes;
    

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		// args should be: IFC2X3_Final, IFC2X3_TC1, IFC4 or IFC4_ADD1, nothing
		// else is accepted here
        URL resourceUrl=Main.class.getResource("/");
        String path=resourceUrl.getPath();
        File[] files=new File(path).listFiles();
        for (File file:files) {
        	String fileName=file.getName();
        	if (fileName.startsWith("IFC4x3")&&fileName.endsWith(".exp")) {
        		fileName=fileName.substring(0,fileName.length()-4);
        	Main main = new Main(fileName);
    		String inAlt = fileName+"/";

    		Namespace.IFC = "http://standards.buildingsmart.org/IFC/DEV/" + inAlt + "OWL";
    		main.er.readAndBuild();

    		main.er.outputEntitiesAndTypes(path+"\\", main.in);
    		main.er.outputEntityPropertyList(path+"\\", main.in);

    		OWLWriter ow = new OWLWriter(main.in, main.er.entities, main.er.types, main.er.getSiblings(),
    				main.er.getEnumIndividuals(), main.er.getProperties(),main.er.entityDerives);
    		ow.outputOWL(path+"\\",main.in);
    		ow.outputOWLSupplement(path+"\\",main.in);  		
  //  		main.writeExcel("D:\\Data\\2020-IFC_LinkedData_PoC\\IFC4X3_1.xls");

    		System.out.println("Ended converting the EXPRESS schema into corresponding OWL file");
        	}
        	
        }
		

	}

	public Main(String version) {
		this.in = version;
		InputStream instr = ExpressReader.class.getResourceAsStream("/" + in + ".exp");
		this.er = new ExpressReader(instr);
		this.workbook=new HSSFWorkbook();
		this.predefinedTypes=new HashMap<String,TypeVO>();
		this.pr=new PSetReader();
		this.par=new PackageReader();
		par.readPackages("D:\\Data\\2020-IFC_LinkedData_PoC\\20200222 alpha proposed 4_3_0_0\\4_3_0_0\\gen\\html\\schema", "D:\\Data\\2020-IFC_LinkedData_PoC\\20200222 alpha proposed 4_3_0_0\\4_3_0_0\\gen");
	}

	public void writeExcel(String file) throws ParserConfigurationException, SAXException {
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			
			
            
			convertEntities("Class_Attribute");
//			convertPredefinedTypes("Class<<PredefinedType>>");
			convertSelectTypes("Interface<<Select>>");
			convertEnumerationTypes("Enumeration");
//			convertEntityHierarchy("Inheritance Tree");
			convertDataTypes("DataType");
			convertProperties("Property");
			convertPEnums("Enumeration<<PEnum>>");
			workbook.write(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void convertEntities(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] {"Layer","Package", "Class(Entity)", "Attribute", "Type",
				"SuperType" };
		fillRow(firstrow, columnNames);

		Map<String, EntityVO> entityMap = er.entities;
		int i = 1;
		for (String layer:par.getLayerPackageMap().keySet()) {
			List<String> packageNames=par.getLayerPackageMap().get(layer);
			for (String pn:packageNames) {
				List<String> entityNames=par.getPackageEntityMap().get(pn);
				for (String en:entityNames) {
					if (entityMap.get(en.toUpperCase())!=null) {
					EntityVO entity = entityMap.get(en.toUpperCase());
					HSSFRow row = sheet.createRow(i);
					i=createEntityRow(layer,pn,row, entity, i, sheet);
					}
				}
			}
		}

		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
	}
	
	public void convertPredefinedTypes(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] { "Layer","Resource","Class<<Entity>>", "Class<<PredefinedType>>"};
		fillRow(firstrow, columnNames);
		Map<String, EntityVO> entityMap = er.entities;
		int i = 1;
		for (String layer:par.getLayerPackageMap().keySet()) {
			List<String> packageNames=par.getLayerPackageMap().get(layer);
			for (String pn:packageNames) {
				List<String> entityNames=par.getPackageEntityMap().get(pn);
				for (String en:entityNames) {
					if (entityMap.get(en.toUpperCase())!=null) {
			EntityVO entity = entityMap.get(en.toUpperCase());
			
			List<AttributeVO> attributes=entity.getAttributes();
			for (AttributeVO attribute:attributes) {
				if (attribute.getName().startsWith("PredefinedType_")) {
					if(!entity.getName().endsWith("Type")) {
					TypeVO type=attribute.getType();
					predefinedTypes.put(type.getName().toUpperCase(),type);
					List<String> enumItems=type.getEnumEntities();
					for (String enumItem:enumItems) {
						if(!(enumItem.equals("USERDEFINED")||enumItem.equals("NOTDEFINED"))) {
						HSSFRow row=sheet.createRow(i);
						fillRow(row,new String[]{layer, pn,entity.getName(),entity.getName()+"."+enumItem});
						i++;
						}
					}}
				}}
				}		
				}
			}
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
	}
	
	private void convertSelectTypes(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] { "Layer","Package","Interface<<Select>>", "SelectItem"};
		fillRow(firstrow, columnNames);
		int i = 1;
		Map<String,TypeVO> types=er.getTypes();
		for (String layer:par.getLayerPackageMap().keySet()) {
			List<String> packageNames=par.getLayerPackageMap().get(layer);
			for (String pn:packageNames) {
				List<String> entityNames=par.getPackageEntityMap().get(pn);
				for (String en:entityNames) {
					if (types.get(en.toUpperCase())!=null) {
			TypeVO type=types.get(en.toUpperCase());
			if (type.getSelectEntities()!=null&&type.getSelectEntities().size()>0) {
				for (String item:type.getSelectEntities()) {
				HSSFRow row=sheet.createRow(i);
				fillRow(row,new String[]{layer,pn,type.getName(),item});
				i++;
				}
			}
					}}}
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);	
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
	}
	
	private void convertDataTypes(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] {"Layer", "Package","DataType", "BaseType"};
		fillRow(firstrow, columnNames);
		int i = 1;
		Map<String,TypeVO> types=er.getTypes();
		for (String layer:par.getLayerPackageMap().keySet()) {
			List<String> packageNames=par.getLayerPackageMap().get(layer);
			for (String pn:packageNames) {
				List<String> entityNames=par.getPackageEntityMap().get(pn);
				for (String en:entityNames) {
					if (types.get(en.toUpperCase())!=null) {
			TypeVO type=types.get(en.toUpperCase());
			if (type.getSelectEntities().size()==0&&type.getEnumEntities().size()==0) {
				HSSFRow row=sheet.createRow(i);
/*				int[] card=type.getListCardinalities();
				int min=card[0];
				int max=card[1];
				String cardinality=new String();
				if (max==-1) {
					cardinality="["+String.valueOf(min)+".."+"*"+"]";
				}else {
					cardinality="["+String.valueOf(min)+".."+String.valueOf(max)+"]";
				}*/
				fillRow(row,new String[] {layer, pn,type.getName(),type.getPrimarytype()});
				i++;
			}
		}
				}}}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);	
		sheet.autoSizeColumn(3);
	}
	
	
	private void convertEntityHierarchy(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		List<EntityVO> roots=new ArrayList<EntityVO>();
		Map<String, EntityVO> entityMap = er.entities;
		int i = 0;int j=0;
		for (String key:entityMap.keySet()) {
			EntityVO entity=entityMap.get(key);
			if (entity.getSuperclass()==null) {
				roots.add(entity);
			}
				
		}
		for (EntityVO root:roots) {
			writeEntityWithSubtypes(sheet,root,i,j);
			i++;
		}
		
	}
	
	private void writeEntityWithSubtypes(HSSFSheet sheet,EntityVO entity, int rownum, int columnnum) {
		HSSFRow row=sheet.createRow(rownum);
		Cell cell=row.createCell(columnnum);
		cell.setCellValue(entity.getName());
		if (entity.getSubClassList()!=null) {
			rownum++;
			columnnum++;
			for (String s:entity.getSubClassList()) {
				EntityVO child=er.entities.get(s.toUpperCase());
				writeEntityWithSubtypes(sheet,child,rownum,columnnum);
				rownum++;
			}
		}
		
	}
	
	
	
	private void convertEnumerationTypes(String sheetname) {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] { "Layer","Resource","Enumeration", "EnumerationItem"};
		fillRow(firstrow, columnNames);
		int i = 1;
		Map<String,TypeVO> types=er.getTypes();
		
		for (String layer:par.getLayerPackageMap().keySet()) {
			List<String> packageNames=par.getLayerPackageMap().get(layer);
			for (String pn:packageNames) {
				List<String> entityNames=par.getPackageEntityMap().get(pn);
				for (String en:entityNames) {
					if (types.get(en.toUpperCase())!=null) {
			TypeVO type=types.get(en.toUpperCase());
			if (type.getEnumEntities()!=null&&type.getEnumEntities().size()>0) {
				if(predefinedTypes.get(type.getName().toUpperCase())==null) {
				for (String item:type.getEnumEntities()) {
				HSSFRow row=sheet.createRow(i);
				if (!(item.equals("USERDEFINED")||item.equals("NOTDEFINED"))) {
				fillRow(row,new String[]{layer,pn,type.getName(),item});
				i++;
				}
				}
				}
			}}}}
			
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);	
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
	}
	
	private void convertProperties(String sheetname) throws ParserConfigurationException, SAXException, IOException {
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] { "Class", "Enumeration","PropertySet","Property","PropertyType","Type"};
		fillRow(firstrow,columnNames);
		int i=1;
		pr.parsePSetList("D:\\Data\\2020-IFC_LinkedData_PoC\\20200222 alpha proposed 4_3_0_0\\4_3_0_0\\gen\\html\\psd");
//		pr.parsePSetList("D:\\Data\\2020-IFC_LinkedData_PoC\\Test");
		Map<String, List<PSet>> map=pr.psetmap;
		List<String> list=new ArrayList<String>(pr.psetmap.keySet());
		list.sort(Comparator.comparing( String::toString ) );
		for(String key:list) {
			List<PSet> psets=map.get(key);
	for (PSet pset:psets) {
		List<Property> properties=pset.getProperties();
		for (Property property:properties) {
			HSSFRow row=sheet.createRow(i);
			if(key.contains("/")) {
				String cn=key.substring(0,key.indexOf("/"));
				String en=key.replace("/", ".");
				fillRow(row,new String[]{cn,en,pset.getName(),property.getName(),property.getType(),property.getDatatype()});
			}else {
			fillRow(row,new String[]{key,"",pset.getName(),property.getName(),property.getType(),property.getDatatype()});
			}
			i++;
		}
	}
			
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		sheet.autoSizeColumn(5);
	}
	
	private void convertPEnums(String sheetname) {
		
		HSSFSheet sheet = workbook.createSheet(sheetname);
		HSSFRow firstrow = sheet.createRow(0);
		String[] columnNames = new String[] { "PEnumType", "PEnumItem"};
		fillRow(firstrow,columnNames);
		int i=1;
		Map<String, PEnum> map=pr.penummap;
		List<String> list=new ArrayList<String>(map.keySet());
		list.sort(Comparator.comparing( String::toString ) );
		for(String key:list) {
			PEnum penum=map.get(key);
	for (String item:penum.getItems()) {
            if (!(item.equals("NOTKNOWN")||item.equals("UNSET")||item.equals("OTHER"))) {
			HSSFRow row=sheet.createRow(i);
			fillRow(row,new String[]{penum.getName(),item});
			i++;
            }
		}
	}
			
		
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);

		
	}
	
	private void headingRowStyle(HSSFRow row) {

	}
	
	private void contentRowStyle1(HSSFRow row) {
		
	}
	
	private void contentRowStyle2(HSSFRow row) {
		
	}

	private int createEntityRow(String layer, String packageName,HSSFRow row, EntityVO entity, int rn, HSSFSheet sheet) {
		String[] entityRow = new String[] { layer,packageName,entity.getName(), "", "", entity.getSuperclass() };
		fillRow(row, entityRow);
		rn++;
		List<AttributeVO> attributes = entity.getAttributes();
		int i=0;
		for (;i<attributes.size();i++) {
			String[] attributeRow = new String[] { layer,packageName,entity.getName(), attributes.get(i).getName(), attributes.get(i).getType().getName()};
			HSSFRow attr = sheet.createRow(rn+i);
			fillRow(attr, attributeRow);
		}
		rn=rn+i;
		return rn;
	}

	private void fillRow(HSSFRow row, String[] values) {
		for (int i = 0; i < values.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(values[i]);
		}
	}

}
