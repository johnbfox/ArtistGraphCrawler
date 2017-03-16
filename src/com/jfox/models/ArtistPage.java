package com.jfox.models;

import java.util.ArrayList;
import java.util.List;

public class ArtistPage {
	private String artistName;
	private PageType pageType;
	private List<String> associatedActsLinks;
	private List<String> membersLinks;
	
	public ArtistPage(){
		associatedActsLinks = new ArrayList<String>();
		membersLinks = new ArrayList<String>();
	}
	
	//Setters
	public void setArtistName(String artistName){ this.artistName = artistName; }
	public void setPageType(PageType pageType){ this.pageType = pageType; }		
	public void addAssociatedActsLink(String associatedAct){ associatedActsLinks.add(associatedAct); }
	public void addMembersLink(String member){	membersLinks.add(member); }
	
	//Getters
	public String getArtistName(){ return artistName; }
	public PageType getPageType(){ return pageType; }
	public List<String> getAssociatedActsLinks(){ return associatedActsLinks; }
	public List<String> getMembersLinks(){	return membersLinks;	}
}
