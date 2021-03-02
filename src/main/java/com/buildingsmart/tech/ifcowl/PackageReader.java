package com.buildingsmart.tech.ifcowl;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageReader {
	
	private Map<String,List<String>> packageEntityMap=new HashMap<String, List<String>>();
	private Map<String, String> entityPackageMap=new HashMap<String, String>();
	private Map<String,List<String>> layerPackageMap=new HashMap<String, List<String>>();
	
	
	
	public void readPackages(String dir1,String dir2) {
		parsePackages(dir1);
		rewritePEMap(dir2);
		readLayers(dir2);
		for (String key:packageEntityMap.keySet()) {
			List<String> list=packageEntityMap.get(key);
			for(String entity:list) {
				entityPackageMap.put(entity, key);
			}
		}
	}
	
	public Map<String, List<String>> getPackageEntityMap() {
		return packageEntityMap;
	}

	public void setPackageEntityMap(Map<String, List<String>> packageEntityMap) {
		this.packageEntityMap = packageEntityMap;
	}

	public Map<String, String> getEntityPackageMap() {
		return entityPackageMap;
	}

	public void setEntityPackageMap(Map<String, String> entityPackageMap) {
		this.entityPackageMap = entityPackageMap;
	}

	public Map<String, List<String>> getLayerPackageMap() {
		return layerPackageMap;
	}

	public void setLayerPackageMap(Map<String, List<String>> layerPackageMap) {
		this.layerPackageMap = layerPackageMap;
	}

	public void readLayers(String directory) {
		File file=new File(directory);
		File[] files=file.listFiles();
		List<String> resource=new ArrayList<String>();
		List<String> core=new ArrayList<String>();
		List<String> shared=new ArrayList<String>();
		List<String> domain=new ArrayList<String>();
		
		for(File f:files) {
			if (f.getName().startsWith("Ifc")) {
				String name=f.getName();
				name=name.substring(0,name.indexOf("."));
				if(name.endsWith("Resource")) {
					resource.add(name);
				}else if(name.startsWith("IfcShared")) {
					shared.add(name);
				}else if(name.equals("IfcKernel")||name.endsWith("Extension")) {
					core.add(name);
				}else {
					domain.add(name);
				}
			}
		}
		
		resource.sort( Comparator.comparing( String::toString ) ); 
		core.sort( Comparator.comparing( String::toString ) ); 
		shared.sort( Comparator.comparing( String::toString ) ); 
		domain.sort( Comparator.comparing( String::toString ) ); 
		
		layerPackageMap.put("Resource",resource);
		layerPackageMap.put("Core",core);
		layerPackageMap.put("Shared",shared);
		layerPackageMap.put("Domain", domain);
	}
	
	public void parsePackages(String directory){
		File file=new File(directory);
		File[] files=file.listFiles();
		for (File f:files) {
			if (f.getName().startsWith("ifc")) {
			    File[] fs=f.listFiles();
			    for (File ff:fs) {
			    	if (ff.getName().equals("lexical")) {
			    		File[] ffs=ff.listFiles();
			    		List<String> list=new ArrayList<String>();
			    		for (File fff:ffs) {
			    			list.add(fff.getName().substring(0,fff.getName().indexOf(".")));
			    		}
			    		packageEntityMap.put(f.getName(), list);
			    	}
			    }
			}
		}
	}
	
	public void rewritePEMap(String directory) {
		File file=new File(directory);
		File[] files=file.listFiles();
		for (File f:files) {
			String name=f.getName();
			if (name.startsWith("Ifc")) {
			name=name.substring(0,name.indexOf("."));
			if (packageEntityMap.get(name.toLowerCase())!=null) {
				List<String> list=packageEntityMap.get(name.toLowerCase());
				packageEntityMap.remove(name.toLowerCase());
				packageEntityMap.put(name, list);
			}
			}
		}
		
		for (String key:packageEntityMap.keySet()) {
			packageEntityMap.get(key).sort( Comparator.comparing( String::toString ) );
		}
	}

}
