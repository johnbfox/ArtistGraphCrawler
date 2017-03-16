package com.jfox.data;

public class DataLayerFactory {
	
	public DatabaseLayer getDataLayer(){
		return new Neo4jLayer();
	}

}
