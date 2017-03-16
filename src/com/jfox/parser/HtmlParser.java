package com.jfox.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jfox.models.ArtistPage;
import com.jfox.models.PageType;

public class HtmlParser {
	
	/*
	 * Method parses a Wikipedia musician or band page and extracts information
	 * Artist, Associated Acts, Members
	 * 
	 */
	public static ArtistPage processDocument(String url){
		
		// Refresh the current page
		ArtistPage currentPage = new ArtistPage();	
		
		// Declare constants
		final String INFO_BOX_CLASS = ".infobox",
					 EXTRANEOUS_HEADER_CONTENT = " - Wikipedia",
					 EMPTY_STRING = "",
					 TABLE_ROW_TAG = "tr",
					 TABLE_HEADER_TAG = "th",
					 LINK_TAG = "a",
					 LINK_URL_ATTRIBUTE = "href",
					 MEMBERS_HEADER = "members",
					 FORMER_MEMBERS_HEADER = "former members",
					 PAST_MEMBERS_HEADER = "past members",
					 ASSOCIATED_ACTS_HEADER = "associated acts",
					 WIKIPEDIA_URL_BASE = "https://en.wikipedia.org";
	
		Document doc;
		
		//Try to grab page hosted at URL
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			return null;
		}
		
		//Extract the musician/band name from the page title
		String artistName = doc.title().replace(EXTRANEOUS_HEADER_CONTENT, EMPTY_STRING);	
		
		// Grab the grey "info table" from the wikipedia page
		Elements infoTable = doc.select(INFO_BOX_CLASS);
		//Pull the rows out of the info table
		Elements rows = infoTable.select(TABLE_ROW_TAG);
		
		currentPage.setArtistName(artistName);
		
		boolean pageHasMembers = false,
				pageHasAssociatedActs = false;
		
		// Iterate throw the rows of the table
		for(Element row : rows){
			// Get text for the row header
			String headerText = row.select(TABLE_HEADER_TAG).text().toLowerCase();			
			
			// Determine if there are member links in the row by checking the header text
			boolean rowHasMembers = false;
			boolean rowHasAssociatedActs = false;
			
			if ( headerText.equals(MEMBERS_HEADER) || headerText.equals(FORMER_MEMBERS_HEADER) ||
					headerText.equals(PAST_MEMBERS_HEADER ) ){
				//set member booleans to true
				rowHasMembers = true;
				pageHasMembers = true;
			}else if (headerText.equals(ASSOCIATED_ACTS_HEADER)){
				//set associated acts booleans to true
				rowHasAssociatedActs = true;
				pageHasAssociatedActs = true;
			}
			
			//Determine if the row has links that should be traversed
			boolean shouldTraverseRow = (rowHasMembers || rowHasAssociatedActs);
			
			// Only traverse links if the row contains members or associated acts
			if(shouldTraverseRow){
				//Extract links from table row
				Elements links = row.select(LINK_TAG);
				
				//Iterate through links
				for (Element link : links){
					// Create full url from link
					String tmpFullUrl = WIKIPEDIA_URL_BASE + link.attr(LINK_URL_ATTRIBUTE);				
					
					// If row contains members, add to the member links
					if(rowHasMembers){
						currentPage.addMembersLink(tmpFullUrl);
					}
					
					// If row has associated acts, add to associated acts links
					if (rowHasAssociatedActs){
						currentPage.addAssociatedActsLink(tmpFullUrl);
					}
				}
			}	
		}
		
		//Returns PageType of current page
		PageType pageType = determinePageType(pageHasMembers, pageHasAssociatedActs);
		//Set the page with the current page type
		currentPage.setPageType(pageType);
		
		// return model containing page data
		return currentPage;
	}
	
	private static PageType determinePageType(boolean pageHasMembers, boolean pageHasAssociatedActs){
		//Page is a band if it has members, artist if it only has associated acts, and unknown otherwise
		if(pageHasMembers){
			return PageType.BAND;
		}else if(pageHasAssociatedActs){
			return PageType.MUSICIAN;
		}else{
			return PageType.UNKNOWN;
		}
	}

}
