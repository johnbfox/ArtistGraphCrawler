# ArtistGraphCrawler

ArtistGraphCrawler is a web crawler designed to extract musical artists and their collaborative network from Wikipedia.  This is designed to persist the acquired data into a neo4j database, but can be adapted to any other database type.

## The algorithm

This scraper uses a variation of Breadth First Search over the graph of musicians, with each musician being a node, and an edge representing a collaboration between two artists.  Since many artists perform in bands, those bands are represented as edges rather than nodes.

## Future enhancements

While the algorithm works well, there are several enhancements that are in the development pipeline.

1) The algorithm currently uses a hashmap as a cache.  While this boosts performance by preventing multiple visits to a single web page, the memory footprint gets larger than necessary as there is no eviction policy.  A simple eviction policy should drastically reduce the amount of memory that the program uses.
2) Only the least amount of data possible is being extracted from the web pages.  The parser could be extended to extract addtional data from the pages.
3) I built this in Java mostly because I'm stronger with it than other server side languages.  That said, this program makes more sense as a Python script.  I'd like to migrate this project over to Python in the future.

## Suggestions?

Any ideas to improve or enhance this crawler are welcome.  Additionally, interesting applications for this data are welcome as well.  Please fork and run your own experiments!  See my visualization of the graph at http://johnbfox.com/musician-graph . Email me at jfox892@gmail.com with any suggestions.  
