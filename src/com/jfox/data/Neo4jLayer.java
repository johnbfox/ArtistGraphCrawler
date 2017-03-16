package com.jfox.data;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.List;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.jfox.models.Connection;
import com.jfox.models.Node;
import com.jfox.utils.ConfigReader;

public class Neo4jLayer implements DatabaseLayer {
	
	private static org.neo4j.driver.v1.Driver driver;
	
	public boolean init(){
		ConfigReader cr = new ConfigReader();
		
		// Configuration property constants
		final String DB_URL_PROPERTY = "db.url",
					 DB_USER_PROPERTY = "db.user",
			         DB_PASSWORD_PROPERTY = "db.password";
		
		// Get properties from url
		String dbUrl = cr.getProperty(DB_URL_PROPERTY),
			   dbUser = cr.getProperty(DB_USER_PROPERTY),
			   dbPassword = cr.getProperty(DB_PASSWORD_PROPERTY);
		
		driver = GraphDatabase.driver( dbUrl, AuthTokens.basic(dbUser, dbPassword) );
		
		try{
			Session session = driver.session();
			session.close();
		}catch(NullPointerException e){
			return false;
		}
		
		return true;
	}
	
	
	// Save an artist and its links to the database
	public void saveArtist(Node n){
		try (Session session = driver.session()){
			try ( Transaction tx = session.beginTransaction() ){
				
				// Add new artist if it doesn't exist
				if(!hasArtist(tx, n.getArtistName())){
					createNewArtist(tx, n.getArtistName());
				}
				//
				List<Connection> connections = n.getConnections();
				for(Connection c : connections){
					if(!hasArtist(tx, c.getName())){
						createNewArtist(tx, c.getName());
					}
					createLink(tx, n.getArtistName(), c.getName());
				}
				tx.success();
				tx.close();
			}
		}
	}
	
	public void close(){
		driver.close();
	}
	
	// Creates a new artist in the database
	public static void createNewArtist(Transaction tx, String name){
		if( ! name.toLowerCase().contains(("Wikipedia, the free encyclopedia".toLowerCase() ) ) ){
			tx.run("CREATE (a:Artist {name:{name}})", parameters("name", name));
		}
	}
	
	// Creates a unique link from one artist to another
	public static void createLink(Transaction tx, String name1, String name2){
		if( !name1.toLowerCase().contains("Wikipedia, the free encyclopedia".toLowerCase()) && !name2.toLowerCase().contains("Wikipedia, the free encyclopedia".toLowerCase()) ){
			tx.run("MATCH (a1:Artist {name:{name1}}), (a2:Artist {name:{name2}}) CREATE UNIQUE (a1)-[:LINK]->(a2)",
					parameters( "name1", name1, "name2", name2 ));
		}
	}
	
	//Checks to see if an artist already exists in the database
	public static boolean hasArtist(Transaction tx, String name){
		StatementResult sr = tx.run( "MATCH (a1:Artist) WHERE a1.name={name} RETURN a1", parameters("name", name) );
		return ( sr.hasNext() );
	}
}
