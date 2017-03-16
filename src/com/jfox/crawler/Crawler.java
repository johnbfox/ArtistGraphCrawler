package com.jfox.crawler;

import java.util.Queue;
import java.util.Set;

import com.jfox.data.DataLayerFactory;
import com.jfox.data.DatabaseLayer;
import com.jfox.models.ArtistPage;
import com.jfox.models.Connection;
import com.jfox.models.Node;
import com.jfox.models.PageType;
import com.jfox.models.QueueArtist;
import com.jfox.parser.HtmlParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Crawler {
	
	// Current artist being visited
	private QueueArtist curArtist;
	private Map<String, Node> artistGraph;
	
	//Primary queue for the crawler, outstanding artists to be crawled
	private Queue<QueueArtist> linkQueue;
	
	//List of completed artist nodes
	private ArrayList<QueueArtist> completed;
	
	//Cache for artist data, prevents http requests if page has been visited
	private Map<String, ArtistPage> artistCache;
	
	private boolean isCrawling = true;
	private boolean isDatabaseValidated;
	
	public Crawler( String baseUrl ){
		//Initialize properties
		linkQueue = new LinkedList<QueueArtist>();
		completed = new ArrayList<QueueArtist>();
		artistCache = new HashMap<String, ArtistPage>();
		
		//Add first artist to queue
		QueueArtist firstArtist = new QueueArtist();
		firstArtist.setToURL(baseUrl);
		linkQueue.add(firstArtist);
		
		//Test the database connection
		DataLayerFactory dlf = new DataLayerFactory();
		DatabaseLayer dl = dlf.getDataLayer();
		isDatabaseValidated = dl.init();
		dl.close();
	}
	
	
	public void crawl(int iterations){
		// Ensure that the database props are valid before crawling
		if(!isDatabaseValidated){
			throw new Error("Database connection failed.  Please ensure that your database properties are correct");
		}else{
			int count = 0;
			isCrawling = count < iterations;
			
			//Crawl until there are no artists left in the queue and the inputted number of iterations hasn't been exceeded
			while( (curArtist = linkQueue.poll()) != null || isCrawling ){
				step();
				count++;
				isCrawling = (count < iterations);
				
				if(isCrawling){
					if(count%100 == 0){
						System.out.println(count + " out of " + iterations + " iterations complete");
					}
				}else if(!isCrawling){
					if( linkQueue.size() % 100 == 0 ){
						System.out.println(linkQueue.size() + " backlog artists left.");
					}
				}
			}
			
			System.out.println("********* COMPLETED ARTISTS *********");
			System.out.println(completed);
			System.out.println();
			
			complete();
			reconcileData();
		}
	}
	
	
	/**
	 * 	Primary logic for stepping through Wikipedia for data
	 * 
	 */
	private void step(){
		//Looks to see if the artist is available from our cache
		ArtistPage ap = artistCache.get( curArtist.getToURL() );
		boolean cacheHit = true;
		
		// If the artist isn't in the cache, get the Webpage from wikipedia 
		if( ap == null ){
		   cacheHit = false;
		   stall(1);
		   
		   ap = HtmlParser.processDocument(curArtist.getToURL());
		   if( ap == null ){
			   return; 
		   }
		   
		   artistCache.put(curArtist.getToURL(), ap);
		}
		
		List<String> links;
		
		/*
		 * Sets links to the appropriate value.  If the page is a musician, set links
		 * to associated acts links.  If the page is a band, set the links to the
		 * member links.  If the page is neither, do nothing and return from function.
		 */
		if(ap.getPageType() == PageType.MUSICIAN){
			links = ap.getAssociatedActsLinks();
			completeCurrentNode(ap);
		}else if(ap.getPageType() == PageType.BAND){
			links = ap.getMembersLinks();
		}else{
			return;
		}
		
		// If the crawler is still crawling
		if(isCrawling){
			//add new nodes from current page to the queue
			addNodesToQueue(ap, links, cacheHit);
		}
		
	}
	
	/*
	 * Add new nodes to the queue
	 */
	private void addNodesToQueue(ArtistPage ap, List<String> links, boolean cacheHit){
		for(String link : links){
			QueueArtist qa = new QueueArtist();
			
			//if the current page is a band
			if(ap.getPageType() == PageType.BAND){
				
				qa.setFromURL(curArtist.getFromURL());
				qa.setArtistName(curArtist.getArtistName());
				qa.setConnection(ap.getArtistName());
				qa.setToURL(link);
				
				linkQueue.add(qa);
			}else{	
				if (!cacheHit){
					qa.setArtistName(ap.getArtistName());
					qa.setFromURL(curArtist.getToURL());
					qa.setToURL(link);
					
					linkQueue.add(qa);
				}
			}			
		}
	}
	
	private void completeCurrentNode (ArtistPage ap){
		curArtist.setToArtist(ap.getArtistName());
		
		if(curArtist.getConnection() == null){
			curArtist.setConnection("Direct");
		}

		if(curArtist.isComplete() && curArtist.isValidNode()){
			completed.add(curArtist);
		}
	}
	
	// Build the graph.  
	private void complete(){
		artistGraph = new HashMap<String, Node>();
		for(QueueArtist an : completed){
			Node n;
			
			if(( n = artistGraph.get(an.getArtistName()) ) == null){
				n = new Node(an.getArtistName());
				n.addConnection(an.getToArtist(), an.getConnection());
				
				artistGraph.put(n.getArtistName(), n);
			}else{
				n.addConnection(an.getToArtist(), an.getConnection());
			}
		}

	}
	
	/*
	 * Reconciles the connections between all artists.  Ensures that if one artist
	 * has another as a connection, then the other should also have that artist
	 * as a connection
	 */
	public void reconcileData(){
		Set<String> artistNames = artistGraph.keySet();
		for(String name : artistNames){
			Node n = artistGraph.get(name);
			
			for(Connection c : n.getConnections()){
				String toArtist = c.getName();
				
				Node n2 = artistGraph.get(toArtist);
				
				
				if(n2 != null){
					boolean hasArtist = false;
					
					for(Connection c2 : n2.getConnections()){
						if(c2.getName().equals(name)){
							hasArtist = true;
						}
					}
					
					if(!hasArtist){
						n2.addConnection(name, c.getConnection());
					}
				}
			}
		}
		System.out.println("*************** RECONCILED DATA *************");
		System.out.println(artistGraph);
		System.out.println();
	}
	
	//Saves the graph to the database
	public void saveData(){
		DataLayerFactory dlf = new DataLayerFactory();
		
		DatabaseLayer nl = dlf.getDataLayer();
		nl.init();
		Set<String> names = artistGraph.keySet();
		
		for(String name : names){
			nl.saveArtist(artistGraph.get(name));
		}
		
		System.out.println(names.size() + " artists saved to db.");
		
		nl.close();
	}
	
	//Helper function to stall for one second.  Prevents wikipedia requests from overloading
	private void stall(int seconds){
		long time0, time1;
		time0 = System.currentTimeMillis();
		do{
			time1 = System.currentTimeMillis();
		}
		while ((time1-time0) < 1000*seconds);
	}
	
}
