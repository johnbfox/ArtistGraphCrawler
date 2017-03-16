package com.jfox.data;

import com.jfox.models.Node;

public interface DatabaseLayer {
	
	public boolean init();
	public void saveArtist(Node n);
	public void close();

}
