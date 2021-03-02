package com.buildingsmart.tech.ifcowl;

import java.util.List;

public class Property {

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	private String name;
	private String type;
	private String description;
	private String datatype;
	private PEnum penum;
	public PEnum getPenum() {
		return penum;
	}
	public void setPenum(PEnum penum) {
		this.penum = penum;
	}
}
