package com.jfox.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
	
	private final String CONFIG_FILE = "resources/config.properties";
	private Properties properties;
	
	public ConfigReader(){
		properties = new Properties();
		
		FileInputStream input = null;
		
		try {
			input = new FileInputStream(CONFIG_FILE);
			properties.load(input);
		} catch (FileNotFoundException e) {
			System.err.println(CONFIG_FILE + " not found!");
		}catch (IOException e){
			System.err.println(CONFIG_FILE + " could not be read!");
		}
	}
	
	public String getProperty(String property){
		return properties.getProperty(property);
	}
}
