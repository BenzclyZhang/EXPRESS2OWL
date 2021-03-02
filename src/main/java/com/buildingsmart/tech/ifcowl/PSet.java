package com.buildingsmart.tech.ifcowl;

import java.util.List;

public class PSet {
	
	private String name;
	private List<Property> properties;
	private String type;
	private String description;
	private List<String> applicableEntities;
	
	public List<String> getApplicableEntities() {
		return applicableEntities;
	}
	public void setApplicableEntities(List<String> applicableEntities) {
		this.applicableEntities = applicableEntities;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Property> getProperties() {
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}
