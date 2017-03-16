package com.jfox;

import com.jfox.crawler.Crawler;

public class Driver {
	
	public static void main(String[] args){
		String startUrl = "https://en.wikipedia.org/wiki/John_Lennon";
		Crawler c  = new Crawler(startUrl);
		
		c.crawl(20000);
		c.saveData();
	}
}
	