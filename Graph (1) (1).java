

import java.io.*;
import java.util.*;


/**
 *Nameera Muhammad Dawood
 * 
 * This file contains the implementation for Assignment 4 where it builds an exponent graph with 
 * edges ranging from 0 to 1000, with appropriate edge weight according to the formula specified 
 * in the assignment. In the same run, the program also finds and prints into a file the shortest paths
 * from vertex 0 to all other vertices
 * 
 * Included within the submission are three other files created by author Mark Allen Weiss
 * required to run Graph.java.
 * More information is included within the writeup.txt file
 */
// Used to signal violations of preconditions for
// various shortest path algorithms.
class GraphException extends RuntimeException
{
	private static final long serialVersionUID = 0L;
	
    public GraphException( String name )
    {
        super( name );
    }
}

// Represents an edge in the graph.
class Edge
{
    public Vertex     dest;   // Second vertex in Edge
    public double     cost;   // Edge cost
    
    public Edge( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
}

// Represents an entry in the priority queue for Dijkstra's algorithm.
class Path implements Comparable<Path>
{
    public Vertex     dest;   // w
    public double     cost;   // d(w)
    
    public Path( Vertex d, double c )
    {
        dest = d;
        cost = c;
    }
    
    public int compareTo( Path rhs )
    {
        double otherCost = rhs.cost;
        
        return cost < otherCost ? -1 : cost > otherCost ? 1 : 0;
    }
}

// Represents a vertex in the graph.
class Vertex
{
    public String     name;   // Vertex name
    public List<Edge> adj;    // Adjacent vertices
    public double     dist;   // Cost
    public Vertex     prev;   // Previous vertex on shortest path
    public int        scratch;// Extra variable used in algorithm
    public int 		  edgeCount;

    public Vertex( String nm )
      { name = nm; adj = new LinkedList<Edge>( ); reset( ); }

    public void reset( )
      { dist = Graph.INFINITY; prev = null; pos = null; scratch = 0; edgeCount = 0;}    
      
    public PairingHeap.Position<Path> pos;  // Used for dijkstra2 (Chapter 23)
}

// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
//                              --> Add additional edge
// void printPath( String w )   --> Print path after alg is run
// void unweighted( String s )  --> Single-source unweighted
// void dijkstra( String s )    --> Single-source weighted
// void negative( String s )    --> Single-source negative weighted
// void acyclic( String s )     --> Single-source acyclic
// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm.  Exceptions are thrown if errors are detected.

public class Graph
{
    public static final double INFINITY = Double.MAX_VALUE;
    private Map<String,Vertex> vertexMap = new HashMap<String,Vertex>( );

    /**
     * Add a new edge to the graph.
     */
    public void addEdge( String sourceName, String destName, double cost )
    {
        Vertex v = getVertex( sourceName );
        Vertex w = getVertex( destName );
        v.adj.add( new Edge( w, cost ) );
    }

    /**
     * Driver routine to handle unreachables and print total cost.
     * It calls recursive routine to print shortest path to
     * destNode after a shortest path algorithm has run.
     * 
     * This method has been edited from its original version for the 
     * purpose of implementing the Assignment's goals.
     */
    public void printPath( String destName, PrintWriter outfile )
    {
        Vertex w = vertexMap.get( destName );
        if( w == null )
            throw new NoSuchElementException( "Destination vertex not found" );
        else if( w.dist == INFINITY )
            outfile.println( destName + " is unreachable" );
        else
        {
            outfile.print( "(Cost is: " + w.dist + ") " );
            printPath( w, outfile );
            outfile.println( );
        }
    }

    /**
     * If vertexName is not present, add it to vertexMap.
     * In either case, return the Vertex.
     */
    private Vertex getVertex( String vertexName )
    {
        Vertex v = vertexMap.get( vertexName );
        if( v == null )
        {
            v = new Vertex( vertexName );
            vertexMap.put( vertexName, v );
        }
        return v;
    }

    /**
     * Recursive routine to print shortest path to dest
     * after running shortest path algorithm to a file. The path
     * is known to exist.
     * 
     * This method has been edited from the original version
     * for the purpose of the assignment
     */
    private void printPath( Vertex dest , PrintWriter outfile)
    {
        if( dest.prev != null )
        {
            printPath( dest.prev, outfile );
            outfile.print( " to " );
        }
        outfile.print( dest.name );
    }
    
    /**
     * Initializes the vertex output info prior to running
     * any shortest path algorithm.
     */
    private void clearAll( )
    {
        for( Vertex v : vertexMap.values( ) )
            v.reset( );
    }

    /**
     * Single-source unweighted shortest-path algorithm.
     */
    public void unweighted( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                if( w.dist == INFINITY )
                {
                    w.dist = v.dist + 1;
                    w.prev = v;
                    q.add( w );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm.
     */
    public void dijkstra( String startName )
    {
        PriorityQueue<Path> pq = new PriorityQueue<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        pq.add( new Path( start, 0 ) ); start.dist = 0;
        
        int nodesSeen = 0;
        while( !pq.isEmpty( ) && nodesSeen < vertexMap.size( ) )
        {
            Path vrec = pq.remove( );
            Vertex v = vrec.dest;
            if( v.scratch != 0 )  // already processed v
                continue;
                
            v.scratch = 1;
            nodesSeen++;

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw || (w.dist == (v.dist + cvw) && (v.edgeCount + 1) < w.edgeCount))
                {
                	w.edgeCount = v.edgeCount + 1;
                	
                    w.dist = v.dist +cvw;
                    w.prev = v;
                    pq.add( new Path( w, w.dist ) );
                }
            }
        }
    }

    /**
     * Single-source weighted shortest-path algorithm using pairing heaps.
     */
    public void dijkstra2( String startName )
    {
        PairingHeap<Path> pq = new PairingHeap<Path>( );

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( );
        start.pos = pq.insert( new Path( start, 0 ) ); start.dist = 0;

        while ( !pq.isEmpty( ) )
        {
            Path vrec = pq.deleteMin( );
            Vertex v = vrec.dest;
                
            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( cvw < 0 )
                    throw new GraphException( "Graph has negative edges" );
                    
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                    
                    Path newVal = new Path( w, w.dist );                    
                    if( w.pos == null )
                        w.pos = pq.insert( newVal );
                    else
                        pq.decreaseKey( w.pos, newVal ); 
                }
            }
        }
    }

    /**
     * Single-source negative-weighted shortest-path algorithm.
     */
    public void negative( String startName )
    {
        clearAll( ); 

        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        Queue<Vertex> q = new LinkedList<Vertex>( );
        q.add( start ); start.dist = 0; start.scratch++;

        while( !q.isEmpty( ) )
        {
            Vertex v = q.remove( );
            if( v.scratch++ > 2 * vertexMap.size( ) )
                throw new GraphException( "Negative cycle detected" );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                      // Enqueue only if not already on the queue
                    if( w.scratch++ % 2 == 0 )
                        q.add( w );
                    else
                        w.scratch--;  // undo the enqueue increment    
                }
            }
        }
    }

    /**
     * Single-source negative-weighted acyclic-graph shortest-path algorithm.
     */
    public void acyclic( String startName )
    {
        Vertex start = vertexMap.get( startName );
        if( start == null )
            throw new NoSuchElementException( "Start vertex not found" );

        clearAll( ); 
        Queue<Vertex> q = new LinkedList<Vertex>( );
        start.dist = 0;
        
          // Compute the indegrees
		Collection<Vertex> vertexSet = vertexMap.values( );
        for( Vertex v : vertexSet )
            for( Edge e : v.adj )
                e.dest.scratch++;
            
          // Enqueue vertices of indegree zero
        for( Vertex v : vertexSet )
            if( v.scratch == 0 )
                q.add( v );
       
        int iterations;
        for( iterations = 0; !q.isEmpty( ); iterations++ )
        {
            Vertex v = q.remove( );

            for( Edge e : v.adj )
            {
                Vertex w = e.dest;
                double cvw = e.cost;
                
                if( --w.scratch == 0 )
                    q.add( w );
                
                if( v.dist == INFINITY )
                    continue;    
                
                if( w.dist > v.dist + cvw )
                {
                    w.dist = v.dist + cvw;
                    w.prev = v;
                }
            }
        }
        
        if( iterations != vertexMap.size( ) )
            throw new GraphException( "Graph has a cycle!" );
    }

    /**
     * Process a request; return false if end of file.
     * 
     * Due to the fact that we would only be utilizing Dijkstra's algorithm in 
     * this assignment, processRequest has been heavily edited for ease of 
     * implementation (what is not included in the new version has been commented out)
     */
    public static boolean processRequest( Graph g, PrintWriter outfile)
    {
        try
        {
 
            String startName = "0";
            for (int i = 1; i <= 1000; i++){
                String destName = Integer.toString(i);
                g.dijkstra(startName);//only need this
                g.printPath(destName, outfile);
            }
        }
        catch( NoSuchElementException e )
          { return false; }
        catch( GraphException e )
          { System.err.println( e ); }
        return true;
    }

    /**
     * TODO: complete a method that constructs a file. This
     * file contains data about vertices and edge costs. 
     */
    public void generateFileForGraph( String inp) {
    	String outputFileName = inp;
    	PrintWriter graphFileExponent = null;
    	
   	    try {
         FileWriter fout = new FileWriter( outputFileName );
         graphFileExponent = new PrintWriter( fout );
         
       	 String line = "";
       	 int i = 0, LIMIT = 1000;
       	 double cost = 0;
		
       	 
       	 
         while(i <= LIMIT-1) {
            /* TODO: write data about vertices (exponents) and 
             * edges when an edge connects an exponent to the 
             * next greater exponent. You may use the 'line' 
             * variable for storing the data.    	 
             */
        	 
        	 cost = i;
        	 line = i + " " + ( i + 1 ) + " " + cost;
             StringTokenizer st = new StringTokenizer( line );
             
             try {
               if( st.countTokens( ) != 3 ) {
                System.err.println( "Skipping ill-formatted line " + line );
                continue;
               }
               String source  = st.nextToken( );
               String dest    = st.nextToken( );
               cost   = Double.parseDouble( st.nextToken( ) );

               graphFileExponent.println( source + " " + dest + " " + cost );
               
             /* TODO: write data about vertices (exponents) and 
              * edges when an edge connects an exponent to its 
              * doubled value. You may use 'line' variable for 
              * storing the data. 	 
              */
               
               if(i == 0 || i == 1) 
               {
            	   i++;
            	   continue; 
               }
               
             
               if(( 2 * i ) <= LIMIT == false)
               {
            	   i++;
            	   continue; 
               }
               
               
               cost = i * (1 + (Math.log(i) / Math.log(2))); //credit : Professor Shervin
               
               line = i + " " + (i + i) + " " + cost;
               
               st = new StringTokenizer( line );
               
               if( st.countTokens( ) != 3 ) {
                   System.err.println( "Skipping ill-formatted line " + line );
                   continue;
               }
               source  = st.nextToken( );
               dest    = st.nextToken( );
               cost    = Double.parseDouble( st.nextToken( ) );                 

               graphFileExponent.println( source + " " + dest + " " + cost );               
               
             } catch( NumberFormatException e ) {
            	 System.err.println( "Skipping ill-formatted line " + line );            	 
             }
             
             i++;
             
         }        
   	    } catch( IOException e ) {
   	    	System.err.println( e );   	    	
   	    } finally {
   	    	if(graphFileExponent != null) {
   	    	 graphFileExponent.close();
   	    	}
   	    }
    }
    /**
     * A main routine that:
     * 1. Reads a file containing edges (supplied as a command-line parameter);
     * 2. Forms the graph;
     * 3. Repeatedly prompts for two vertices and
     *    runs the shortest path algorithm.
     * The data file is a sequence of lines of the format
     *    source destination cost
     */
    public static void main( String [ ] args )
    {
        Graph g = new Graph( );
        /* TODO: call the generateFileForGraph method here. 
         */
        g.generateFileForGraph(args[0]);
        
        Scanner graphFile = null;
        try
        {
            FileReader fin = new FileReader( args[0] );
            graphFile = new Scanner( fin );

            // Read the edges and insert
            String line;
            while( graphFile.hasNextLine( ) )
            {
                line = graphFile.nextLine( );
                StringTokenizer st = new StringTokenizer( line );

                try
                {
                    if( st.countTokens( ) != 3 )
                    {
                        System.err.println( "Skipping ill-formatted line " + line );
                        continue;
                    }
                    String source  = st.nextToken( );
                    String dest    = st.nextToken( );
                    double cost    = Double.parseDouble( st.nextToken( ) );
                    g.addEdge( source, dest, cost );
                }
                catch( NumberFormatException e )
                  { System.err.println( "Skipping ill-formatted line " + line ); }
             }
         }
         catch( IOException e )
           { System.err.println( e ); }
        finally {
        	if(graphFile != null)
        		graphFile.close();
        }

         System.out.println( "File read..." );
         System.out.println( g.vertexMap.size( ) + " vertices" );
         
         /* TODO: declare a file for printing the shortest 
          * paths. Pass the file, as another argument, to 
          * processRequest to display all the shortest paths. 
          * Each shortest path is displayed after 
          * returning from the Dijkstra's algorithm.
          * 
          * Note: You do not need System.in to read the names 
          * of vertices from the command prompt.
          */ 
         PrintWriter graphoutput = null;
         FileWriter shortest = null;
         try
         {
        	 shortest = new FileWriter( args[1] );
             graphoutput = new PrintWriter( shortest );
             
             // Scanner in = new Scanner( System.in );
             processRequest(g, graphoutput);
         } catch( IOException e ) {
	    	System.err.println( e );   	    	
	    } finally {
	    	if(graphoutput != null) {
	    		graphoutput.close();
	    	}
	    } // finally ends

    } // main ends
} // class ends
/*

graph:
0 1 0.0
1 2 1.0
2 3 2.0
2 4 4.0
3 4 3.0
3 6 7.754887502163468
4 5 4.0
4 8 12.0
5 6 5.0
5 10 16.609640474436812
6 7 6.0
6 12 21.509775004326936
7 8 7.0
7 14 26.651484454403228
8 9 8.0
8 16 32.0
9 10 9.0
9 18 37.529325012980806
10 11 10.0
10 20 43.219280948873624
11 12 11.0
11 22 49.05374780501028
12 13 12.0
12 24 55.01955000865387
13 14 13.0
13 26 61.1057163358342
14 15 14.0
14 28 67.30296890880645
15 16 15.0
15 30 73.60335893412778
16 17 16.0
16 32 80.0
17 18 17.0
17 34 86.48686830125578
18 19 18.0
18 36 93.05865002596161
19 20 19.0
19 38 99.71062275542812
20 21 20.0
20 40 106.43856189774725
21 22 21.0
21 42 113.23866587835397
22 23 22.0
22 44 120.10749561002054
23 24 23.0
23 46 127.0419249893113
24 25 24.0
24 48 134.03910001730776
25 26 25.0
25 50 141.0964047443681
26 27 26.0
26 52 148.2114326716684
27 28 27.0
27 54 155.38196255841368
28 29 28.0
28 56 162.6059378176129
29 30 29.0
29 58 169.8814488586996
30 31 30.0
30 60 177.20671786825557
31 32 31.0
31 62 184.58008562199316
32 33 32.0
32 64 192.0
33 34 33.0
33 66 199.46500593882897
34 35 34.0
34 68 206.97373660251156
35 36 35.0
35 70 214.5249055930738
36 37 36.0
36 72 222.11730005192322
37 38 37.0
37 74 229.74977452827116
38 39 38.0
38 76 237.42124551085624
39 40 39.0
39 78 245.1306865356277
40 41 40.0
40 80 252.8771237954945
41 42 41.0
41 82 260.65963218934144
42 43 42.0
42 84 268.47733175670794
43 44 43.0
43 86 276.3293844521902
44 45 44.0
44 88 284.2149912200411
45 46 45.0
45 90 292.1333893348354
46 47 46.0
46 92 300.0838499786226
47 48 47.0
47 94 308.06567602884894
48 49 48.0
48 96 316.0782000346155
49 50 49.0
49 98 324.1207823616452
50 51 50.0
50 100 332.1928094887362
51 52 51.0
51 102 340.2936924405463
52 53 52.0
52 104 348.4228653433368
53 54 53.0
53 106 356.5797840918496
54 55 54.0
54 108 364.76392511682735
55 56 55.0
55 110 372.9747842438563
56 57 56.0
56 112 381.21187563522585
57 58 57.0
57 114 389.4747308073903
58 59 58.0
58 116 397.76289771739914
59 60 59.0
59 118 406.07593991234864
60 61 60.0
60 120 414.41343573651113
61 62 61.0
61 122 422.7749775913361
62 63 62.0
62 124 431.1601712439863
63 64 63.0
63 126 439.56863518049477
64 65 64.0
64 128 448.0
65 66 65.0
65 130 456.4539078468495
66 67 66.0
66 132 464.93001187765793
67 68 67.0
67 134 473.42797576067073
68 69 68.0
68 136 481.94747320502313
69 70 69.0
69 138 490.48818751769375
70 71 70.0
70 140 499.04981118614774
71 72 71.0
71 142 507.6320454848324
72 73 72.0
72 144 516.2346001038464
73 74 73.0
73 146 524.8571927982413
74 75 74.0
74 148 533.4995490565424
75 76 75.0
75 150 542.161401787191
76 77 76.0
76 152 550.8424910217126
77 78 77.0
77 154 559.5425636335075
78 79 78.0
78 156 568.2613730712554
79 80 79.0
79 158 576.9986791059912
80 81 80.0
80 160 585.7542475909889
81 82 81.0
81 162 594.5278502336546
82 83 82.0
82 164 603.319264378683
83 84 83.0
83 166 612.1282728017948
84 85 84.0
84 168 620.9546635134159
85 86 85.0
85 170 629.7982295717047
86 87 86.0
86 172 638.6587689043804
87 88 87.0
87 174 647.5360841388394
88 89 88.0
88 176 656.4299824400822
89 90 89.0
89 178 665.3402753560094
90 91 90.0
90 180 674.2667786696708
91 92 91.0
91 182 683.2093122580814
92 93 92.0
92 184 692.1676999572452
93 94 93.0
93 186 701.141769433047
94 95 94.0
94 188 710.1313520576979
95 96 95.0
95 190 719.1362827914401
96 97 96.0
96 192 728.156400069231
97 98 97.0
97 194 737.1915456921514
98 99 98.0
98 196 746.2415647232905
99 100 99.0
99 198 755.3063053878814
100 101 100.0
100 200 764.3856189774725
101 102 101.0
101 202 773.4793597579313
102 103 102.0
102 204 782.5873848810925
103 104 103.0
103 206 791.7095542998715
104 105 104.0
104 208 800.8457306866735
105 106 105.0
105 210 809.9957793549429
106 107 106.0
106 212 819.1595681836991
107 108 107.0
107 214 828.3369675449227
108 109 108.0
108 216 837.5278502336547
109 110 109.0
109 218 846.7320914006849
110 111 110.0
110 220 855.9495684877126
111 112 111.0
111 222 865.1801611648618
112 113 112.0
112 224 874.4237512704516
113 114 113.0
113 226 883.6802227529163
114 115 114.0
114 228 892.9494616147806
115 116 115.0
115 230 902.2313558586033
116 117 116.0
116 232 911.5257954347983
117 118 117.0
117 234 920.8326721912583
118 119 118.0
118 236 930.1518798246973
119 120 119.0
119 238 939.4833138336453
120 121 120.0
120 240 948.8268714730223
121 122 121.0
121 242 958.182451710226
122 123 122.0
122 244 967.5499551826722
123 124 123.0
123 246 976.9292841567265
124 125 124.0
124 248 986.3203424879727
125 126 125.0
125 250 995.723035582761
126 127 126.0
126 252 1005.1372703609895
127 128 127.0
127 254 1014.5629552200651
128 129 128.0
128 256 1024.0
129 130 129.0
129 258 1033.4483159495999
130 131 130.0
130 260 1042.9078156936991
131 132 131.0
131 262 1052.378413201406
132 133 132.0
132 264 1061.860023755316
133 134 133.0
133 266 1071.3525639216584
134 135 134.0
134 268 1080.8559515213415
135 136 135.0
135 270 1090.3701056018622
136 137 136.0
136 272 1099.894946410046
137 138 137.0
137 274 1109.4303953655922
138 139 138.0
138 276 1118.9763750353875
139 140 139.0
139 278 1128.5328091085678
140 141 140.0
140 280 1138.0996223722955
141 142 141.0
141 282 1147.67674068823
142 143 142.0
142 284 1157.2640909696647
143 144 143.0
143 286 1166.8616011593097
144 145 144.0
144 288 1176.469200207693
145 146 145.0
145 290 1186.0868180521654
146 147 146.0
146 292 1195.7143855964825
147 148 147.0
147 294 1205.3518346909457
148 149 148.0
148 296 1214.9990981130845
149 150 149.0
149 298 1224.656109548862
150 151 150.0
150 300 1234.3228035743823
151 152 151.0
151 302 1243.9991156380872
152 153 152.0
152 304 1253.684982043425
153 154 153.0
153 306 1263.3803399319756
154 155 154.0
154 308 1273.085127267015
155 156 155.0
155 310 1282.799282817507
156 157 156.0
156 312 1292.5227461425109
157 158 157.0
157 314 1302.2554575759855
158 159 158.0
158 316 1311.9973582119824
159 160 159.0
159 318 1321.7483898902124
160 161 160.0
160 320 1331.5084951819779
161 162 161.0
161 322 1341.2776173764535
162 163 162.0
162 324 1351.0557004673092
163 164 163.0
163 326 1360.842689139666
164 165 164.0
164 328 1370.638528757366
165 166 165.0
165 330 1380.4431653505596
166 167 166.0
166 332 1390.2565456035895
167 168 167.0
167 334 1400.0786168431669
168 169 168.0
168 336 1409.9093270268318
169 170 169.0
169 338 1419.7486247316892
170 171 170.0
170 340 1429.5964591434094
171 172 171.0
171 342 1439.4527800454887
172 173 172.0
172 344 1449.3175378087608
173 174 173.0
173 346 1459.1906833811536
174 175 174.0
174 348 1469.0721682776789
175 176 175.0
175 350 1478.9619445706576
176 177 176.0
176 352 1488.8599648801642
177 178 177.0
177 354 1498.7661823646904
178 179 178.0
178 356 1508.6805507120187
179 180 179.0
179 358 1518.603024130302
180 181 180.0
180 360 1528.5335573393413
181 182 181.0
181 362 1538.4721055620603
182 183 182.0
182 364 1548.4186245161627
183 184 183.0
183 366 1558.37307040598
184 185 184.0
184 368 1568.3353999144902
185 186 185.0
185 370 1578.3055701955175
186 187 186.0
186 372 1588.2835388660937
187 188 187.0
187 374 1598.2692639989882
188 189 188.0
188 376 1608.2627041153958
189 190 189.0
189 378 1618.2638181777827
190 191 190.0
190 380 1628.27256558288
191 192 191.0
191 382 1638.288906154828
192 193 192.0
192 384 1648.312800138462
193 194 193.0
193 386 1658.3442081927394
194 195 194.0
194 388 1668.3830913843026
195 196 195.0
195 390 1678.4294111811741
196 197 196.0
196 392 1688.4831294465807
197 198 197.0
197 394 1698.5442084329063
198 199 198.0
198 396 1708.6126107757625
199 200 199.0
199 398 1718.6882994881862
200 201 200.0
200 400 1728.7712379549448
201 202 201.0
201 402 1738.8613899269646
202 203 202.0
202 404 1748.9587195158626
203 204 203.0
203 406 1759.0631911885907
204 205 204.0
204 408 1769.174769762185
205 206 205.0
205 410 1779.2934203986167
206 207 206.0
206 412 1789.419108599743
207 208 207.0
207 414 1799.55180020236
208 209 208.0
208 416 1809.6914613733472
209 210 209.0
209 418 1819.8380586049043
210 211 210.0
210 420 1829.9915587098856
211 212 211.0
211 422 1840.1519288172162
212 213 212.0
212 424 1850.3191363673984
213 214 213.0
213 426 1860.4931491081038
214 215 214.0
214 428 1870.6739350898454
215 216 215.0
215 430 1880.8614626617339
216 217 216.0
216 432 1891.0557004673096
217 218 217.0
217 434 1901.256617440452
218 219 218.0
218 436 1911.4641828013696
219 220 219.0
219 438 1921.678366052657
220 221 220.0
220 440 1931.8991369754256
221 222 221.0
221 442 1942.1264656255064
222 223 222.0
222 444 1952.3603223297237
223 224 223.0
223 446 1962.600677682228
224 225 224.0
224 448 1972.8475025409032
225 226 225.0
225 450 1983.1007680238333
226 227 226.0
226 452 1993.3604455058326
227 228 227.0
227 454 2003.626506615038
228 229 228.0
228 456 2013.8989232295614
229 230 229.0
229 458 2024.1776674742005
230 231 230.0
230 460 2034.4627117172065
231 232 231.0
231 462 2044.754028567109
232 233 232.0
232 464 2055.051590869597
233 234 233.0
233 466 2065.3553717044474
234 235 234.0
234 468 2075.6653443825167
235 236 235.0
235 470 2085.9814824427754
236 237 236.0
236 472 2096.303759649395
237 238 237.0
237 474 2106.6321499888877
238 239 238.0
238 476 2116.9666276672906
239 240 239.0
239 478 2127.307167107399
240 241 240.0
240 480 2137.6537429460445
241 242 241.0
241 482 2148.006330031421
242 243 242.0
242 484 2158.3649034204523
243 244 243.0
243 486 2168.729438376205
244 245 244.0
244 488 2179.0999103653444
245 246 245.0
245 490 2189.4762950556305
246 247 246.0
246 492 2199.858568313453
247 248 247.0
247 494 2210.2467062014152
248 249 248.0
248 496 2220.6406849759455
249 250 249.0
249 498 2231.0404810849523
250 251 250.0
250 500 2241.446071165522
251 252 251.0
251 502 2251.8574320416437
252 253 252.0
252 504 2262.274540721979
253 254 253.0
253 506 2272.6973743976605
254 255 254.0
254 508 2283.1259104401306
255 256 255.0
255 510 2293.560126399009
256 257 256.0
256 512 2304.0
257 258 257.0
257 514 2314.4455091428267
258 259 258.0
258 516 2324.8966318991997
259 260 259.0
259 518 2335.353346510817
260 261 260.0
260 520 2345.8156313873983
261 262 261.0
261 522 2356.28346510474
262 263 262.0
262 524 2366.756826402812
263 264 263.0
263 526 2377.2356941838752
264 265 264.0
264 528 2387.720047510632
265 266 265.0
265 530 2398.209865604399
266 267 266.0
266 532 2408.705127843317
267 268 267.0
267 534 2419.2058137605773
268 269 268.0
268 536 2429.711903042683
269 270 269.0
269 538 2440.223375527732
270 271 270.0
270 540 2450.7402112037244
271 272 271.0
271 542 2461.2623902068995
272 273 272.0
272 544 2471.7898928200925
273 274 273.0
273 546 2482.32269947112
274 275 274.0
274 548 2492.8607907311844
275 276 275.0
275 550 2503.4041473133066
276 277 276.0
276 552 2513.9527500707745
277 278 277.0
277 554 2524.506579995625
278 279 278.0
278 556 2535.0656182171347
279 280 279.0
279 558 2545.629846000343
280 281 280.0
280 560 2556.1992447445905
281 282 281.0
281 562 2566.77379598208
282 283 282.0
282 564 2577.35348137646
283 284 283.0
283 566 2587.9382827214226
284 285 284.0
284 568 2598.52818193933
285 286 285.0
285 570 2609.12316107985
286 287 286.0
286 572 2619.7232023186193
287 288 287.0
287 574 2630.328287955923
288 289 288.0
288 576 2640.938400415386
289 290 289.0
289 578 2651.5535222426965
290 291 290.0
290 580 2662.173636104331
291 292 291.0
291 582 2672.7987247863107
292 293 292.0
292 584 2683.428771192965
293 294 293.0
293 586 2694.0637583457187
294 295 294.0
294 588 2704.703669381891
295 296 295.0
295 590 2715.348487553515
296 297 296.0
296 592 2725.998196226169
297 298 297.0
297 594 2736.6527788778276
298 299 298.0
298 596 2747.312219097724
299 300 299.0
299 598 2757.9765005852337
300 301 300.0
300 600 2768.6456071487646
301 302 301.0
301 602 2779.3195227046704
302 303 302.0
302 604 2789.9982312761736
303 304 303.0
303 606 2800.681716992304
304 305 304.0
304 608 2811.36996408685
305 306 305.0
305 610 2822.062956897326
306 307 306.0
306 612 2832.7606798639513
307 308 307.0
307 614 2843.463117528645
308 309 308.0
308 616 2854.170254534029
309 310 309.0
309 618 2864.882075622452
310 311 310.0
310 620 2875.598565635014
311 312 311.0
311 622 2886.3197095106175
312 313 312.0
312 624 2897.0454922850217
313 314 313.0
313 626 2907.7758990899097
314 315 314.0
314 628 2918.5109151519705
315 316 315.0
315 630 2929.250525791993
316 317 316.0
316 632 2939.9947164239647
317 318 317.0
317 634 2950.743472554192
318 319 318.0
318 636 2961.496779780425
319 320 319.0
319 638 2972.2546237909933
320 321 320.0
320 640 2983.0169903639558
321 322 321.0
321 642 2993.7838653662593
322 323 322.0
322 644 3004.555234752907
323 324 323.0
323 646 3015.3310845661376
324 325 324.0
324 648 3026.1114009346184
325 326 325.0
325 650 3036.8961700726404
326 327 326.0
326 652 3047.685378279331
327 328 327.0
327 654 3058.479011937873
328 329 328.0
328 656 3069.2770575147315
329 330 329.0
329 658 3080.079501558894
330 331 330.0
330 660 3090.8863307011193
331 332 331.0
331 662 3101.697531653189
332 333 332.0
332 664 3112.5130912071786
333 334 333.0
333 666 3123.3329962347307
334 335 334.0
334 668 3134.1572336863337
335 336 335.0
335 670 3144.9857905906206
336 337 336.0
336 672 3155.8186540536635
337 338 337.0
337 674 3166.6558112582866
338 339 338.0
338 676 3177.4972494633785
339 340 339.0
339 678 3188.3429560032205
340 341 340.0
340 680 3199.192918286819
341 342 341.0
341 682 3210.047123797243
342 343 342.0
342 684 3220.905560090977
343 344 343.0
343 686 3231.768214797274
344 345 344.0
344 688 3242.6350756175216
345 346 345.0
345 690 3253.5061303246084
346 347 346.0
346 692 3264.381366762307
347 348 347.0
347 694 3275.2607728446565
348 349 348.0
348 696 3286.1443365553578
349 350 349.0
349 698 3297.032045947169
350 351 350.0
350 700 3307.923889141315
351 352 351.0
351 702 3318.819854326901
352 353 352.0
352 704 3329.7199297603283
353 354 353.0
353 706 3340.624103764727
354 355 354.0
354 708 3351.532364729381
355 356 355.0
355 710 3362.444701109176
356 357 356.0
356 712 3373.3611014240378
357 358 357.0
357 714 3384.2815542583885
358 359 358.0
358 716 3395.206048260604
359 360 359.0
359 718 3406.1345721424755
360 361 360.0
360 720 3417.067114678683
361 362 361.0
361 722 3428.0036647062684
362 363 362.0
362 724 3438.9442111241206
363 364 363.0
363 726 3449.8887428924577
364 365 364.0
364 728 3460.8372490323254
365 366 365.0
365 730 3471.7897186250934
366 367 366.0
366 732 3482.74614081196
367 368 367.0
367 734 3493.7065047934593
368 369 368.0
368 736 3504.670799828981
369 370 369.0
369 738 3515.639015236286
370 371 370.0
370 740 3526.611140391036
371 372 371.0
371 742 3537.5871647263175
372 373 372.0
372 744 3548.5670777321875
373 374 373.0
373 746 3559.550868955204
374 375 374.0
374 748 3570.5385279979764
375 376 375.0
375 750 3581.5300445187163
376 377 376.0
376 752 3592.5254082307915
377 378 377.0
377 754 3603.5246089022867
378 379 378.0
378 756 3614.527636355566
379 380 379.0
379 758 3625.534480466844
380 381 380.0
380 760 3636.5451311657607
381 382 381.0
381 762 3647.559578434956
382 383 382.0
382 764 3658.577812309656
383 384 383.0
383 766 3669.5998228772587
384 385 384.0
384 768 3680.625600276924
385 386 385.0
385 770 3691.6551346991723
386 387 386.0
386 772 3702.6884163854793
387 388 387.0
387 774 3713.725435627887
388 389 388.0
388 776 3724.766182768606
389 390 389.0
389 778 3735.8106481996283
390 391 390.0
390 780 3746.8588223623483
391 392 391.0
391 782 3757.910695747175
392 393 392.0
392 784 3768.9662588931615
393 394 393.0
393 786 3780.025502387632
394 395 394.0
394 788 3791.0884168658126
395 396 395.0
395 790 3802.154993010464
396 397 396.0
396 792 3813.225221551526
397 398 397.0
397 794 3824.299093265755
398 399 398.0
398 796 3835.3765989763724
399 400 399.0
399 798 3846.4577295527165
400 401 400.0
400 800 3857.54247590989
401 402 401.0
401 802 3868.6308290084235
402 403 402.0
402 804 3879.7227798539293
403 404 403.0
403 806 3890.818319496771
404 405 404.0
404 808 3901.9174390317253
405 406 405.0
405 810 3913.0201295976544
406 407 406.0
406 812 3924.1263823771815
407 408 407.0
407 814 3935.236188596363
408 409 408.0
408 816 3946.34953952437
409 410 409.0
409 818 3957.4664264731755
410 411 410.0
410 820 3968.5868407972334
411 412 411.0
411 822 3979.710773893172
412 413 412.0
412 824 3990.838217199486
413 414 413.0
413 826 4001.969162196231
414 415 414.0
414 828 4013.103600404721
415 416 415.0
415 830 4024.2415233872293
416 417 416.0
416 832 4035.3829227466945
417 418 417.0
417 834 4046.5277901264253
418 419 418.0
418 836 4057.6761172098095
419 420 419.0
419 838 4068.827895720028
420 421 420.0
420 840 4079.9831174197716
421 422 421.0
421 842 4091.1417741109526
422 423 422.0
422 844 4102.3038576344325
423 424 423.0
423 846 4113.469359869739
424 425 424.0
424 848 4124.638272734796
425 426 425.0
425 850 4135.810588185653
426 427 426.0
426 852 4146.986298216208
427 428 427.0
427 854 4158.16539485795
428 429 428.0
428 856 4169.34787017969
429 430 429.0
429 858 4180.533716287305
430 431 430.0
430 860 4191.722925323467
431 432 431.0
431 862 4202.915489467401
432 433 432.0
432 864 4214.111400934619
433 434 433.0
433 866 4225.310651976672
434 435 434.0
434 868 4236.513234880905
435 436 435.0
435 870 4247.719141970199
436 437 436.0
436 872 4258.92836560274
437 438 437.0
437 874 4270.140898171762
438 439 438.0
438 876 4281.356732105314
439 440 439.0
439 878 4292.575859866022
440 441 440.0
440 880 4303.798273950851
441 442 441.0
441 882 4315.023966890867
442 443 442.0
442 884 4326.252931251013
443 444 443.0
443 886 4337.485159629873
444 445 444.0
444 888 4348.720644659446
445 446 445.0
445 890 4359.959379004923
446 447 446.0
446 892 4371.201355364456
447 448 447.0
447 894 4382.446566468943
448 449 448.0
448 896 4393.695005081807
449 450 449.0
449 898 4404.946663998776
450 451 450.0
450 900 4416.201536047667
451 452 451.0
451 902 4427.459614088178
452 453 452.0
452 904 4438.720891011665
453 454 453.0
453 906 4449.985359740945
454 455 454.0
454 908 4461.253013230076
455 456 455.0
455 910 4472.523844464156
456 457 456.0
456 912 4483.797846459122
457 458 457.0
457 914 4495.075012261538
458 459 458.0
458 916 4506.355334948401
459 460 459.0
459 918 4517.638807626939
460 461 460.0
460 920 4528.925423434413
461 462 461.0
461 922 4540.2151755379255
462 463 462.0
462 924 4551.508057134219
463 464 463.0
463 926 4562.80406144949
464 465 464.0
464 928 4574.103181739194
465 466 465.0
465 930 4585.4054112878575
466 467 466.0
466 932 4596.710743408895
467 468 467.0
467 934 4608.019171444413
468 469 468.0
468 936 4619.330688765033
469 470 469.0
469 938 4630.645288769711
470 471 470.0
470 940 4641.96296488555
471 472 471.0
471 942 4653.283710567622
472 473 472.0
472 944 4664.607519298789
473 474 473.0
473 946 4675.934384589534
474 475 474.0
474 948 4687.264299977775
475 476 475.0
475 950 4698.597259028697
476 477 476.0
476 952 4709.933255334581
477 478 477.0
477 954 4721.272282514629
478 479 478.0
478 956 4732.614334214798
479 480 479.0
479 958 4743.95940410763
480 481 480.0
480 960 4755.307485892089
481 482 481.0
481 962 4766.65857329339
482 483 482.0
482 964 4778.012660062841
483 484 483.0
483 966 4789.369739977677
484 485 484.0
484 968 4800.729806840904
485 486 485.0
485 970 4812.092854481128
486 487 486.0
486 972 4823.458876752409
487 488 487.0
487 974 4834.827867534101
488 489 488.0
488 976 4846.199820730689
489 490 489.0
489 978 4857.574730271643
490 491 490.0
490 980 4868.95259011126
491 492 491.0
491 982 4880.333394228514
492 493 492.0
492 984 4891.717136626906
493 494 493.0
493 986 4903.10381133431
494 495 494.0
494 988 4914.4934124028305
495 496 495.0
495 990 4925.885933908651
496 497 496.0
496 992 4937.281369951891
497 498 497.0
497 994 4948.679714656457
498 499 498.0
498 996 4960.0809621699045
499 500 499.0
499 998 4971.485106663293
500 501 500.0
500 1000 4982.892142331044
501 502 501.0
502 503 502.0
503 504 503.0
504 505 504.0
505 506 505.0
506 507 506.0
507 508 507.0
508 509 508.0
509 510 509.0
510 511 510.0
511 512 511.0
512 513 512.0
513 514 513.0
514 515 514.0
515 516 515.0
516 517 516.0
517 518 517.0
518 519 518.0
519 520 519.0
520 521 520.0
521 522 521.0
522 523 522.0
523 524 523.0
524 525 524.0
525 526 525.0
526 527 526.0
527 528 527.0
528 529 528.0
529 530 529.0
530 531 530.0
531 532 531.0
532 533 532.0
533 534 533.0
534 535 534.0
535 536 535.0
536 537 536.0
537 538 537.0
538 539 538.0
539 540 539.0
540 541 540.0
541 542 541.0
542 543 542.0
543 544 543.0
544 545 544.0
545 546 545.0
546 547 546.0
547 548 547.0
548 549 548.0
549 550 549.0
550 551 550.0
551 552 551.0
552 553 552.0
553 554 553.0
554 555 554.0
555 556 555.0
556 557 556.0
557 558 557.0
558 559 558.0
559 560 559.0
560 561 560.0
561 562 561.0
562 563 562.0
563 564 563.0
564 565 564.0
565 566 565.0
566 567 566.0
567 568 567.0
568 569 568.0
569 570 569.0
570 571 570.0
571 572 571.0
572 573 572.0
573 574 573.0
574 575 574.0
575 576 575.0
576 577 576.0
577 578 577.0
578 579 578.0
579 580 579.0
580 581 580.0
581 582 581.0
582 583 582.0
583 584 583.0
584 585 584.0
585 586 585.0
586 587 586.0
587 588 587.0
588 589 588.0
589 590 589.0
590 591 590.0
591 592 591.0
592 593 592.0
593 594 593.0
594 595 594.0
595 596 595.0
596 597 596.0
597 598 597.0
598 599 598.0
599 600 599.0
600 601 600.0
601 602 601.0
602 603 602.0
603 604 603.0
604 605 604.0
605 606 605.0
606 607 606.0
607 608 607.0
608 609 608.0
609 610 609.0
610 611 610.0
611 612 611.0
612 613 612.0
613 614 613.0
614 615 614.0
615 616 615.0
616 617 616.0
617 618 617.0
618 619 618.0
619 620 619.0
620 621 620.0
621 622 621.0
622 623 622.0
623 624 623.0
624 625 624.0
625 626 625.0
626 627 626.0
627 628 627.0
628 629 628.0
629 630 629.0
630 631 630.0
631 632 631.0
632 633 632.0
633 634 633.0
634 635 634.0
635 636 635.0
636 637 636.0
637 638 637.0
638 639 638.0
639 640 639.0
640 641 640.0
641 642 641.0
642 643 642.0
643 644 643.0
644 645 644.0
645 646 645.0
646 647 646.0
647 648 647.0
648 649 648.0
649 650 649.0
650 651 650.0
651 652 651.0
652 653 652.0
653 654 653.0
654 655 654.0
655 656 655.0
656 657 656.0
657 658 657.0
658 659 658.0
659 660 659.0
660 661 660.0
661 662 661.0
662 663 662.0
663 664 663.0
664 665 664.0
665 666 665.0
666 667 666.0
667 668 667.0
668 669 668.0
669 670 669.0
670 671 670.0
671 672 671.0
672 673 672.0
673 674 673.0
674 675 674.0
675 676 675.0
676 677 676.0
677 678 677.0
678 679 678.0
679 680 679.0
680 681 680.0
681 682 681.0
682 683 682.0
683 684 683.0
684 685 684.0
685 686 685.0
686 687 686.0
687 688 687.0
688 689 688.0
689 690 689.0
690 691 690.0
691 692 691.0
692 693 692.0
693 694 693.0
694 695 694.0
695 696 695.0
696 697 696.0
697 698 697.0
698 699 698.0
699 700 699.0
700 701 700.0
701 702 701.0
702 703 702.0
703 704 703.0
704 705 704.0
705 706 705.0
706 707 706.0
707 708 707.0
708 709 708.0
709 710 709.0
710 711 710.0
711 712 711.0
712 713 712.0
713 714 713.0
714 715 714.0
715 716 715.0
716 717 716.0
717 718 717.0
718 719 718.0
719 720 719.0
720 721 720.0
721 722 721.0
722 723 722.0
723 724 723.0
724 725 724.0
725 726 725.0
726 727 726.0
727 728 727.0
728 729 728.0
729 730 729.0
730 731 730.0
731 732 731.0
732 733 732.0
733 734 733.0
734 735 734.0
735 736 735.0
736 737 736.0
737 738 737.0
738 739 738.0
739 740 739.0
740 741 740.0
741 742 741.0
742 743 742.0
743 744 743.0
744 745 744.0
745 746 745.0
746 747 746.0
747 748 747.0
748 749 748.0
749 750 749.0
750 751 750.0
751 752 751.0
752 753 752.0
753 754 753.0
754 755 754.0
755 756 755.0
756 757 756.0
757 758 757.0
758 759 758.0
759 760 759.0
760 761 760.0
761 762 761.0
762 763 762.0
763 764 763.0
764 765 764.0
765 766 765.0
766 767 766.0
767 768 767.0
768 769 768.0
769 770 769.0
770 771 770.0
771 772 771.0
772 773 772.0
773 774 773.0
774 775 774.0
775 776 775.0
776 777 776.0
777 778 777.0
778 779 778.0
779 780 779.0
780 781 780.0
781 782 781.0
782 783 782.0
783 784 783.0
784 785 784.0
785 786 785.0
786 787 786.0
787 788 787.0
788 789 788.0
789 790 789.0
790 791 790.0
791 792 791.0
792 793 792.0
793 794 793.0
794 795 794.0
795 796 795.0
796 797 796.0
797 798 797.0
798 799 798.0
799 800 799.0
800 801 800.0
801 802 801.0
802 803 802.0
803 804 803.0
804 805 804.0
805 806 805.0
806 807 806.0
807 808 807.0
808 809 808.0
809 810 809.0
810 811 810.0
811 812 811.0
812 813 812.0
813 814 813.0
814 815 814.0
815 816 815.0
816 817 816.0
817 818 817.0
818 819 818.0
819 820 819.0
820 821 820.0
821 822 821.0
822 823 822.0
823 824 823.0
824 825 824.0
825 826 825.0
826 827 826.0
827 828 827.0
828 829 828.0
829 830 829.0
830 831 830.0
831 832 831.0
832 833 832.0
833 834 833.0
834 835 834.0
835 836 835.0
836 837 836.0
837 838 837.0
838 839 838.0
839 840 839.0
840 841 840.0
841 842 841.0
842 843 842.0
843 844 843.0
844 845 844.0
845 846 845.0
846 847 846.0
847 848 847.0
848 849 848.0
849 850 849.0
850 851 850.0
851 852 851.0
852 853 852.0
853 854 853.0
854 855 854.0
855 856 855.0
856 857 856.0
857 858 857.0
858 859 858.0
859 860 859.0
860 861 860.0
861 862 861.0
862 863 862.0
863 864 863.0
864 865 864.0
865 866 865.0
866 867 866.0
867 868 867.0
868 869 868.0
869 870 869.0
870 871 870.0
871 872 871.0
872 873 872.0
873 874 873.0
874 875 874.0
875 876 875.0
876 877 876.0
877 878 877.0
878 879 878.0
879 880 879.0
880 881 880.0
881 882 881.0
882 883 882.0
883 884 883.0
884 885 884.0
885 886 885.0
886 887 886.0
887 888 887.0
888 889 888.0
889 890 889.0
890 891 890.0
891 892 891.0
892 893 892.0
893 894 893.0
894 895 894.0
895 896 895.0
896 897 896.0
897 898 897.0
898 899 898.0
899 900 899.0
900 901 900.0
901 902 901.0
902 903 902.0
903 904 903.0
904 905 904.0
905 906 905.0
906 907 906.0
907 908 907.0
908 909 908.0
909 910 909.0
910 911 910.0
911 912 911.0
912 913 912.0
913 914 913.0
914 915 914.0
915 916 915.0
916 917 916.0
917 918 917.0
918 919 918.0
919 920 919.0
920 921 920.0
921 922 921.0
922 923 922.0
923 924 923.0
924 925 924.0
925 926 925.0
926 927 926.0
927 928 927.0
928 929 928.0
929 930 929.0
930 931 930.0
931 932 931.0
932 933 932.0
933 934 933.0
934 935 934.0
935 936 935.0
936 937 936.0
937 938 937.0
938 939 938.0
939 940 939.0
940 941 940.0
941 942 941.0
942 943 942.0
943 944 943.0
944 945 944.0
945 946 945.0
946 947 946.0
947 948 947.0
948 949 948.0
949 950 949.0
950 951 950.0
951 952 951.0
952 953 952.0
953 954 953.0
954 955 954.0
955 956 955.0
956 957 956.0
957 958 957.0
958 959 958.0
959 960 959.0
960 961 960.0
961 962 961.0
962 963 962.0
963 964 963.0
964 965 964.0
965 966 965.0
966 967 966.0
967 968 967.0
968 969 968.0
969 970 969.0
970 971 970.0
971 972 971.0
972 973 972.0
973 974 973.0
974 975 974.0
975 976 975.0
976 977 976.0
977 978 977.0
978 979 978.0
979 980 979.0
980 981 980.0
981 982 981.0
982 983 982.0
983 984 983.0
984 985 984.0
985 986 985.0
986 987 986.0
987 988 987.0
988 989 988.0
989 990 989.0
990 991 990.0
991 992 991.0
992 993 992.0
993 994 993.0
994 995 994.0
995 996 995.0
996 997 996.0
997 998 997.0
998 999 998.0
999 1000 999.0




Output:
(Cost is: 0.0) 0 to 1
(Cost is: 1.0) 0 to 1 to 2
(Cost is: 3.0) 0 to 1 to 2 to 3
(Cost is: 5.0) 0 to 1 to 2 to 4
(Cost is: 9.0) 0 to 1 to 2 to 4 to 5
(Cost is: 10.754887502163468) 0 to 1 to 2 to 3 to 6
(Cost is: 16.75488750216347) 0 to 1 to 2 to 3 to 6 to 7
(Cost is: 17.0) 0 to 1 to 2 to 4 to 8
(Cost is: 25.0) 0 to 1 to 2 to 4 to 8 to 9
(Cost is: 25.609640474436812) 0 to 1 to 2 to 4 to 5 to 10
(Cost is: 35.609640474436816) 0 to 1 to 2 to 4 to 5 to 10 to 11
(Cost is: 32.2646625064904) 0 to 1 to 2 to 3 to 6 to 12
(Cost is: 44.2646625064904) 0 to 1 to 2 to 3 to 6 to 12 to 13
(Cost is: 43.4063719565667) 0 to 1 to 2 to 3 to 6 to 7 to 14
(Cost is: 57.4063719565667) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15
(Cost is: 49.0) 0 to 1 to 2 to 4 to 8 to 16
(Cost is: 65.0) 0 to 1 to 2 to 4 to 8 to 16 to 17
(Cost is: 62.529325012980806) 0 to 1 to 2 to 4 to 8 to 9 to 18
(Cost is: 80.5293250129808) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19
(Cost is: 68.82892142331043) 0 to 1 to 2 to 4 to 5 to 10 to 20
(Cost is: 88.82892142331043) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21
(Cost is: 84.6633882794471) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22
(Cost is: 106.6633882794471) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23
(Cost is: 87.28421251514428) 0 to 1 to 2 to 3 to 6 to 12 to 24
(Cost is: 111.28421251514428) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25
(Cost is: 105.37037884232461) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26
(Cost is: 131.3703788423246) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27
(Cost is: 110.70934086537315) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28
(Cost is: 138.70934086537315) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29
(Cost is: 131.00973089069447) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30
(Cost is: 161.00973089069447) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31
(Cost is: 129.0) 0 to 1 to 2 to 4 to 8 to 16 to 32
(Cost is: 161.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33
(Cost is: 151.48686830125578) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34
(Cost is: 185.48686830125578) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35
(Cost is: 155.58797503894243) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36
(Cost is: 191.58797503894243) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37
(Cost is: 180.2399477684089) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38
(Cost is: 218.2399477684089) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39
(Cost is: 175.26748332105768) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40
(Cost is: 215.26748332105768) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41
(Cost is: 202.0675873016644) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42
(Cost is: 244.0675873016644) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43
(Cost is: 204.77088388946765) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44
(Cost is: 248.77088388946765) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45
(Cost is: 233.7053132687584) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46
(Cost is: 279.7053132687584) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47
(Cost is: 221.32331253245204) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48
(Cost is: 269.32331253245206) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49
(Cost is: 252.38061725951238) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50
(Cost is: 302.3806172595124) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51
(Cost is: 253.58181151399302) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52
(Cost is: 305.581811513993) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53
(Cost is: 286.75234140073826) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54
(Cost is: 340.75234140073826) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55
(Cost is: 273.31527868298605) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56
(Cost is: 329.31527868298605) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57
(Cost is: 308.59078972407275) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58
(Cost is: 366.59078972407275) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59
(Cost is: 308.21644875895004) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60
(Cost is: 368.21644875895004) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61
(Cost is: 345.58981651268766) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62
(Cost is: 407.58981651268766) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63
(Cost is: 321.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64
(Cost is: 385.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65
(Cost is: 360.465005938829) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66
(Cost is: 426.465005938829) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67
(Cost is: 358.4606049037674) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68
(Cost is: 426.4606049037674) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69
(Cost is: 400.0117738943296) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70
(Cost is: 470.0117738943296) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71
(Cost is: 377.70527509086565) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72
(Cost is: 449.70527509086565) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73
(Cost is: 421.33774956721356) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74
(Cost is: 495.33774956721356) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75
(Cost is: 417.66119327926515) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76
(Cost is: 493.66119327926515) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77
(Cost is: 463.37063430403657) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78
(Cost is: 541.3706343040366) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79
(Cost is: 428.14460711655215) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80
(Cost is: 508.14460711655215) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81
(Cost is: 475.9271155103991) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82
(Cost is: 557.9271155103991) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83
(Cost is: 470.54491905837233) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84
(Cost is: 554.5449190583723) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85
(Cost is: 520.3969717538546) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86
(Cost is: 606.3969717538546) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87
(Cost is: 488.98587510950875) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88
(Cost is: 576.9858751095087) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89
(Cost is: 540.9042732243031) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90
(Cost is: 630.9042732243031) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91
(Cost is: 533.789163247381) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92
(Cost is: 625.789163247381) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93
(Cost is: 587.7709892976073) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94
(Cost is: 681.7709892976073) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95
(Cost is: 537.4015125670676) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96
(Cost is: 633.4015125670676) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97
(Cost is: 593.4440948940972) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98
(Cost is: 691.4440948940972) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99
(Cost is: 584.5734267482486) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100
(Cost is: 684.5734267482486) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101
(Cost is: 642.6743097000588) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102
(Cost is: 744.6743097000588) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103
(Cost is: 602.0046768573299) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104
(Cost is: 706.0046768573299) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105
(Cost is: 662.1615956058426) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106
(Cost is: 768.1615956058426) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107
(Cost is: 651.5162665175656) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108
(Cost is: 759.5162665175656) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109
(Cost is: 713.7271256445945) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110
(Cost is: 823.7271256445945) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111
(Cost is: 654.5271543182118) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112
(Cost is: 766.5271543182118) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113
(Cost is: 718.7900094903764) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114
(Cost is: 832.7900094903764) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115
(Cost is: 706.353687441472) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116
(Cost is: 822.353687441472) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117
(Cost is: 772.6667296364215) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118
(Cost is: 890.6667296364215) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119
(Cost is: 722.6298844954612) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120
(Cost is: 842.6298844954612) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121
(Cost is: 790.9914263502861) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122
(Cost is: 912.9914263502861) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123
(Cost is: 776.749987756674) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124
(Cost is: 900.749987756674) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125
(Cost is: 847.1584516931824) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126
(Cost is: 973.1584516931824) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127
(Cost is: 769.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128
(Cost is: 897.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129
(Cost is: 841.4539078468495) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130
(Cost is: 971.4539078468495) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131
(Cost is: 825.3950178164869) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132
(Cost is: 957.3950178164869) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133
(Cost is: 899.8929816994997) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134
(Cost is: 1033.8929816994996) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135
(Cost is: 840.4080781087905) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136
(Cost is: 976.4080781087905) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137
(Cost is: 916.9487924214611) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138
(Cost is: 1054.9487924214611) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139
(Cost is: 899.0615850804774) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140
(Cost is: 1039.0615850804775) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141
(Cost is: 977.6438193791621) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142
(Cost is: 1119.643819379162) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143
(Cost is: 893.9398751947122) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144
(Cost is: 1037.9398751947122) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145
(Cost is: 974.562467889107) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146
(Cost is: 1120.562467889107) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147
(Cost is: 954.8372986237559) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148
(Cost is: 1102.837298623756) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149
(Cost is: 1037.4991513544046) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150
(Cost is: 1187.4991513544046) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151
(Cost is: 968.5036843009777) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152
(Cost is: 1120.5036843009777) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153
(Cost is: 1053.2037569127726) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154
(Cost is: 1207.2037569127726) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155
(Cost is: 1031.632007375292) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156
(Cost is: 1187.632007375292) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157
(Cost is: 1118.3693134100276) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158
(Cost is: 1276.3693134100276) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159
(Cost is: 1013.8988547075411) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160
(Cost is: 1173.898854707541) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161
(Cost is: 1102.6724573502067) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162
(Cost is: 1264.6724573502067) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163
(Cost is: 1079.2463798890822) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164
(Cost is: 1243.2463798890822) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165
(Cost is: 1170.0553883121938) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166
(Cost is: 1336.0553883121938) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167
(Cost is: 1091.4995825717883) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168
(Cost is: 1259.4995825717883) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169
(Cost is: 1184.343148630077) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170
(Cost is: 1354.343148630077) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171
(Cost is: 1159.055740658235) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172
(Cost is: 1331.055740658235) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173
(Cost is: 1253.933055892694) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174
(Cost is: 1427.933055892694) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175
(Cost is: 1145.415857549591) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176
(Cost is: 1321.415857549591) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177
(Cost is: 1242.3261504655181) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178
(Cost is: 1420.3261504655181) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179
(Cost is: 1215.171051893974) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180
(Cost is: 1395.171051893974) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181
(Cost is: 1314.1135854823845) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182
(Cost is: 1496.1135854823845) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183
(Cost is: 1225.9568632046262) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184
(Cost is: 1409.9568632046262) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185
(Cost is: 1326.930932680428) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186
(Cost is: 1512.930932680428) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187
(Cost is: 1297.9023413553052) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188
(Cost is: 1485.9023413553052) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189
(Cost is: 1400.9072720890474) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190
(Cost is: 1590.9072720890474) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191
(Cost is: 1265.5579126362986) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192
(Cost is: 1457.5579126362986) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193
(Cost is: 1370.593058259219) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194
(Cost is: 1564.593058259219) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195
(Cost is: 1339.6856596173877) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196
(Cost is: 1535.6856596173877) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197
(Cost is: 1446.7504002819787) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198
(Cost is: 1644.7504002819787) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199
(Cost is: 1348.9590457257211) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200
(Cost is: 1548.9590457257211) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201
(Cost is: 1458.05278650618) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202
(Cost is: 1660.05278650618) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203
(Cost is: 1425.2616945811512) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204
(Cost is: 1629.2616945811512) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205
(Cost is: 1536.3838639999303) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206
(Cost is: 1742.3838639999303) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207
(Cost is: 1402.8504075440032) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208
(Cost is: 1610.8504075440032) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209
(Cost is: 1516.0004562122726) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210
(Cost is: 1726.0004562122726) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211
(Cost is: 1481.3211637895417) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212
(Cost is: 1693.3211637895417) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213
(Cost is: 1596.4985631507652) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214
(Cost is: 1810.4985631507652) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215
(Cost is: 1489.0441167512204) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216
(Cost is: 1705.0441167512204) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217
(Cost is: 1606.2483579182503) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218
(Cost is: 1824.2483579182503) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219
(Cost is: 1569.6766941323071) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220
(Cost is: 1789.6766941323071) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221
(Cost is: 1688.9072868094563) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222
(Cost is: 1910.9072868094563) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223
(Cost is: 1528.9509055886633) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224
(Cost is: 1752.9509055886633) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225
(Cost is: 1650.2073770711281) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226
(Cost is: 1876.2073770711281) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227
(Cost is: 1611.739471105157) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228
(Cost is: 1839.739471105157) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229
(Cost is: 1735.0213653489795) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230
(Cost is: 1965.0213653489795) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231
(Cost is: 1617.8794828762702) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232
(Cost is: 1849.8794828762702) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233
(Cost is: 1743.1863596327303) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234
(Cost is: 1977.1863596327303) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235
(Cost is: 1702.8186094611187) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236
(Cost is: 1938.8186094611187) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237
(Cost is: 1830.1500434700667) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238
(Cost is: 2068.1500434700665) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239
(Cost is: 1671.4567559684833) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240
(Cost is: 1911.4567559684833) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241
(Cost is: 1800.8123362056872) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242
(Cost is: 2042.8123362056872) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243
(Cost is: 1758.5413815329584) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244
(Cost is: 2002.5413815329584) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245
(Cost is: 1889.9207105070127) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246
(Cost is: 2135.9207105070127) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247
(Cost is: 1763.0703302446468) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248
(Cost is: 2011.0703302446468) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249
(Cost is: 1896.473023339435) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250
(Cost is: 2146.473023339435) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 251
(Cost is: 1852.295722054172) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252
(Cost is: 2104.295722054172) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252 to 253
(Cost is: 1987.7214069132474) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254
(Cost is: 2241.7214069132474) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254 to 255
(Cost is: 1793.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256
(Cost is: 2049.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256 to 257
(Cost is: 1930.4483159495999) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258
(Cost is: 2188.4483159496) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258 to 259
(Cost is: 1884.3617235405486) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260
(Cost is: 2144.3617235405486) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260 to 261
(Cost is: 2023.8323210482554) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262
(Cost is: 2285.8323210482554) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262 to 263
(Cost is: 1887.2550415718028) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264
(Cost is: 2151.255041571803) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264 to 265
(Cost is: 2028.7475817381453) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266
(Cost is: 2294.7475817381455) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266 to 267
(Cost is: 1980.748933220841) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268
(Cost is: 2248.748933220841) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268 to 269
(Cost is: 2124.263087301362) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270
(Cost is: 2394.263087301362) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270 to 271
(Cost is: 1940.3030245188365) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272
(Cost is: 2212.3030245188365) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272 to 273
(Cost is: 2085.8384734743827) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274
(Cost is: 2359.8384734743827) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274 to 275
(Cost is: 2035.9251674568486) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276
(Cost is: 2311.9251674568486) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276 to 277
(Cost is: 2183.481601530029) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278
(Cost is: 2461.481601530029) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278 to 279
(Cost is: 2037.161207452773) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280
(Cost is: 2317.161207452773) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280 to 281
(Cost is: 2186.7383257687075) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282
(Cost is: 2468.7383257687075) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282 to 283
(Cost is: 2134.9079103488266) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284
(Cost is: 2418.9079103488266) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284 to 285
(Cost is: 2286.5054205384718) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286
(Cost is: 2572.5054205384718) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286 to 287
(Cost is: 2070.409075402405) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288
(Cost is: 2358.409075402405) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288 to 289
(Cost is: 2224.0266932468776) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290
(Cost is: 2514.0266932468776) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290 to 291
(Cost is: 2170.2768534855895) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292
(Cost is: 2462.2768534855895) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292 to 293
(Cost is: 2325.9143025800527) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294
(Cost is: 2619.9143025800527) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294 to 295
(Cost is: 2169.8363967368405) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296
(Cost is: 2465.8363967368405) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296 to 297
(Cost is: 2327.493408172618) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298
(Cost is: 2625.493408172618) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298 to 299
(Cost is: 2271.821954928787) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300
(Cost is: 2571.821954928787) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300 to 301
(Cost is: 2431.498266992492) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302
(Cost is: 2733.498266992492) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302 to 303
(Cost is: 2222.188666344403) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304
(Cost is: 2526.188666344403) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304 to 305
(Cost is: 2383.8840242329534) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306
(Cost is: 2689.8840242329534) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306 to 307
(Cost is: 2326.2888841797876) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308
(Cost is: 2634.2888841797876) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308 to 309
(Cost is: 2490.00303973028) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310
(Cost is: 2800.00303973028) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310 to 311
(Cost is: 2324.154753517803) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312
(Cost is: 2636.154753517803) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312 to 313
(Cost is: 2489.8874649512773) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314
(Cost is: 2803.8874649512773) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314 to 315
(Cost is: 2430.36667162201) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316
(Cost is: 2746.36667162201) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316 to 317
(Cost is: 2598.11770330024) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318
(Cost is: 2916.11770330024) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318 to 319
(Cost is: 2345.407349889519) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320
(Cost is: 2665.407349889519) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320 to 321
(Cost is: 2515.1764720839947) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322
(Cost is: 2837.1764720839947) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322 to 323
(Cost is: 2453.728157817516) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324
(Cost is: 2777.728157817516) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324 to 325
(Cost is: 2625.5151464898727) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326
(Cost is: 2951.5151464898727) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326 to 327
(Cost is: 2449.884908646448) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328
(Cost is: 2777.884908646448) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328 to 329
(Cost is: 2623.689545239642) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330
(Cost is: 2953.689545239642) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330 to 331
(Cost is: 2560.3119339157834) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332
(Cost is: 2892.3119339157834) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332 to 333
(Cost is: 2736.1340051553607) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334
(Cost is: 3070.1340051553607) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334 to 335
(Cost is: 2501.40890959862) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336
(Cost is: 2837.40890959862) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336 to 337
(Cost is: 2679.2482073034776) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338
(Cost is: 3017.2482073034776) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338 to 339
(Cost is: 2613.9396077734864) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340
(Cost is: 2953.9396077734864) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340 to 341
(Cost is: 2793.795928675566) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342
(Cost is: 3135.795928675566) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342 to 343
(Cost is: 2608.3732784669955) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344
(Cost is: 2952.3732784669955) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344 to 345
(Cost is: 2790.2464240393883) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346
(Cost is: 3136.2464240393883) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346 to 347
(Cost is: 2723.005224170373) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348
(Cost is: 3071.005224170373) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348 to 349
(Cost is: 2906.8950004633516) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350
(Cost is: 3256.8950004633516) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350 to 351
(Cost is: 2634.275822429755) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352
(Cost is: 2986.275822429755) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352 to 353
(Cost is: 2820.1820399142816) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354
(Cost is: 3174.1820399142816) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354 to 355
(Cost is: 2751.006701177537) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356
(Cost is: 3107.006701177537) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356 to 357
(Cost is: 2938.92917459582) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358
(Cost is: 3296.92917459582) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358 to 359
(Cost is: 2743.7046092333153) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360
(Cost is: 3103.7046092333153) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360 to 361
(Cost is: 2933.6431574560343) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362
(Cost is: 3295.6431574560343) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362 to 363
(Cost is: 2862.532209998547) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364
(Cost is: 3226.532209998547) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364 to 365
(Cost is: 3054.486655888364) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366
(Cost is: 3420.486655888364) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366 to 367
(Cost is: 2794.2922631191163) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368
(Cost is: 3162.2922631191163) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368 to 369
(Cost is: 2988.262433400144) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370
(Cost is: 3358.262433400144) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370 to 371
(Cost is: 2915.214471546522) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372
(Cost is: 3287.214471546522) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372 to 373
(Cost is: 3111.200196679416) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374
(Cost is: 3485.200196679416) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374 to 375
(Cost is: 2906.165045470701) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376
(Cost is: 3282.165045470701) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376 to 377
(Cost is: 3104.166159533088) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378
(Cost is: 3482.166159533088) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378 to 379
(Cost is: 3029.179837671927) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380
(Cost is: 3409.179837671927) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380 to 381
(Cost is: 3229.1961782438757) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382
(Cost is: 3611.1961782438757) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382 to 383
(Cost is: 2913.8707127747607) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384
(Cost is: 3297.8707127747607) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384 to 385
(Cost is: 3115.902120829038) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386
(Cost is: 3501.902120829038) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386 to 387
(Cost is: 3038.9761496435217) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388
(Cost is: 3426.9761496435217) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388 to 389
(Cost is: 3243.0224694403933) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390
(Cost is: 3633.0224694403933) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390 to 391
(Cost is: 3028.1687890639687) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392
(Cost is: 3420.1687890639687) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392 to 393
(Cost is: 3234.229868050294) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394
(Cost is: 3628.229868050294) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394 to 395
(Cost is: 3155.3630110577415) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396
(Cost is: 3551.3630110577415) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396 to 397
(Cost is: 3363.438699770165) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398
(Cost is: 3761.438699770165) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398 to 399
(Cost is: 3077.7302836806657) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400
(Cost is: 3477.7302836806657) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400 to 401
(Cost is: 3287.820435652686) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402
(Cost is: 3689.820435652686) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402 to 403
(Cost is: 3207.0115060220423) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404
(Cost is: 3611.0115060220423) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404 to 405
(Cost is: 3419.1159776947707) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406
(Cost is: 3825.1159776947707) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406 to 407
(Cost is: 3194.4364643433364) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408
(Cost is: 3602.4364643433364) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408 to 409
(Cost is: 3408.555114979768) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410
(Cost is: 3818.555114979768) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410 to 411
(Cost is: 3325.802972599673) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412
(Cost is: 3737.802972599673) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412 to 413
(Cost is: 3541.9356642022904) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414
(Cost is: 3955.9356642022904) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414 to 415
(Cost is: 3212.5418689173503) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416
(Cost is: 3628.5418689173503) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416 to 417
(Cost is: 3430.6884661489075) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418
(Cost is: 3848.6884661489075) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418 to 419
(Cost is: 3345.992014922158) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420
(Cost is: 3765.992014922158) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420 to 421
(Cost is: 3566.152385029489) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422
(Cost is: 3988.152385029489) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422 to 423
(Cost is: 3331.64030015694) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424
(Cost is: 3755.64030015694) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424 to 425
(Cost is: 3553.8143128976453) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426
(Cost is: 3979.8143128976453) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426 to 427
(Cost is: 3467.1724982406104) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428
(Cost is: 3895.1724982406104) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428 to 429
(Cost is: 3691.360025812499) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430
(Cost is: 4121.360025812499) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430 to 431
(Cost is: 3380.09981721853) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432
(Cost is: 3812.09981721853) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432 to 433
(Cost is: 3606.3007341916723) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434
(Cost is: 4040.3007341916723) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434 to 435
(Cost is: 3517.71254071962) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436
(Cost is: 3953.71254071962) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436 to 437
(Cost is: 3745.9267239709075) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438
(Cost is: 4183.926723970908) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438 to 439
(Cost is: 3501.5758311077325) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440
(Cost is: 3941.5758311077325) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440 to 441
(Cost is: 3731.803159757814) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442
(Cost is: 4173.803159757814) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442 to 443
(Cost is: 3641.26760913918) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444
(Cost is: 4085.26760913918) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444 to 445
(Cost is: 3873.507964491684) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446
(Cost is: 4319.507964491684) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446 to 447
(Cost is: 3501.7984081295663) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448
(Cost is: 3949.7984081295663) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448 to 449
(Cost is: 3736.0516736124964) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450
(Cost is: 4186.051673612496) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450 to 451
(Cost is: 3643.5678225769607) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452
(Cost is: 4095.5678225769607) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452 to 453
(Cost is: 3879.833883686166) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454
(Cost is: 4333.833883686166) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454 to 455
(Cost is: 3625.638394334718) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456
(Cost is: 4081.638394334718) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456 to 457
(Cost is: 3863.9171385793575) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458
(Cost is: 4321.917138579358) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458 to 459
(Cost is: 3769.484077066186) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460
(Cost is: 4229.484077066186) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460 to 461
(Cost is: 4009.7753939160884) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462
(Cost is: 4471.775393916088) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462 to 463
(Cost is: 3672.9310737458673) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464
(Cost is: 4136.931073745867) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464 to 465
(Cost is: 3915.2348545807176) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466
(Cost is: 4381.234854580718) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466 to 467
(Cost is: 3818.851704015247) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468
(Cost is: 4286.851704015247) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468 to 469
(Cost is: 4063.1678420755056) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470
(Cost is: 4533.167842075505) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470 to 471
(Cost is: 3799.122369110514) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472
(Cost is: 4271.122369110513) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472 to 473
(Cost is: 4045.4507594500064) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474
(Cost is: 4519.450759450006) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474 to 475
(Cost is: 3947.116671137357) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476
(Cost is: 4423.116671137357) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476 to 477
(Cost is: 4195.457210577466) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478
(Cost is: 4673.457210577466) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478 to 479
(Cost is: 3809.110498914528) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480
(Cost is: 4289.110498914528) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480 to 481
(Cost is: 4059.4630859999043) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482
(Cost is: 4541.463085999904) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482 to 483
(Cost is: 3959.1772396261395) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484
(Cost is: 4443.177239626139) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484 to 485
(Cost is: 4211.541774581892) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486
(Cost is: 4697.541774581892) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486 to 487
(Cost is: 3937.641291898303) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488
(Cost is: 4425.641291898302) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488 to 489
(Cost is: 4192.017676588589) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490
(Cost is: 4682.017676588589) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490 to 491
(Cost is: 4089.7792788204656) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492
(Cost is: 4581.779278820466) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492 to 493
(Cost is: 4346.1674167084275) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494
(Cost is: 4840.1674167084275) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494 to 495
(Cost is: 3983.7110152205923) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496
(Cost is: 4479.711015220592) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496 to 497
(Cost is: 4242.1108113295995) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498
(Cost is: 4740.1108113295995) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498 to 499
(Cost is: 4137.919094504957) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 500
(Cost is: 4637.919094504957) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 500 to 501
(Cost is: 4398.3304553810785) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 251 to 502
(Cost is: 4900.3304553810785) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 251 to 502 to 503
(Cost is: 4114.570262776151) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252 to 504
(Cost is: 4618.570262776151) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252 to 504 to 505
(Cost is: 4376.993096451833) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252 to 253 to 506
(Cost is: 4882.993096451833) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 252 to 253 to 506 to 507
(Cost is: 4270.8473173533785) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254 to 508
(Cost is: 4778.8473173533785) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254 to 508 to 509
(Cost is: 4535.281533312256) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254 to 255 to 510
(Cost is: 5045.281533312256) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 63 to 126 to 127 to 254 to 255 to 510 to 511
(Cost is: 4097.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256 to 512
(Cost is: 4609.0) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256 to 512 to 513
(Cost is: 4363.445509142826) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256 to 257 to 514
(Cost is: 4877.445509142826) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 256 to 257 to 514 to 515
(Cost is: 4255.3449478488) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258 to 516
(Cost is: 4771.3449478488) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258 to 516 to 517
(Cost is: 4523.801662460417) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258 to 259 to 518
(Cost is: 5041.801662460417) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 128 to 129 to 258 to 259 to 518 to 519
(Cost is: 4230.177354927947) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260 to 520
(Cost is: 4750.177354927947) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260 to 520 to 521
(Cost is: 4500.645188645289) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260 to 261 to 522
(Cost is: 5022.645188645289) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 260 to 261 to 522 to 523
(Cost is: 4390.589147451067) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262 to 524
(Cost is: 4914.589147451067) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262 to 524 to 525
(Cost is: 4663.068015232131) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262 to 263 to 526
(Cost is: 5189.068015232131) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 64 to 65 to 130 to 131 to 262 to 263 to 526 to 527
(Cost is: 4274.975089082434) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264 to 528
(Cost is: 4802.975089082434) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264 to 528 to 529
(Cost is: 4549.464907176201) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264 to 265 to 530
(Cost is: 5079.464907176201) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 264 to 265 to 530 to 531
(Cost is: 4437.452709581462) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266 to 532
(Cost is: 4969.452709581462) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266 to 532 to 533
(Cost is: 4713.953395498723) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266 to 267 to 534
(Cost is: 5247.953395498723) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 132 to 133 to 266 to 267 to 534 to 535
(Cost is: 4410.4608362635245) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268 to 536
(Cost is: 4946.4608362635245) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268 to 536 to 537
(Cost is: 4688.972308748573) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268 to 269 to 538
(Cost is: 5226.972308748573) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 268 to 269 to 538 to 539
(Cost is: 4575.003298505086) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270 to 540
(Cost is: 5115.003298505086) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270 to 540 to 541
(Cost is: 4855.525477508261) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270 to 271 to 542
(Cost is: 5397.525477508261) 0 to 1 to 2 to 4 to 8 to 16 to 32 to 33 to 66 to 67 to 134 to 135 to 270 to 271 to 542 to 543
(Cost is: 4412.092917338929) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272 to 544
(Cost is: 4956.092917338929) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272 to 544 to 545
(Cost is: 4694.625723989957) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272 to 273 to 546
(Cost is: 5240.625723989957) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 272 to 273 to 546 to 547
(Cost is: 4578.699264205567) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274 to 548
(Cost is: 5126.699264205567) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274 to 548 to 549
(Cost is: 4863.242620787689) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274 to 275 to 550
(Cost is: 5413.242620787689) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 136 to 137 to 274 to 275 to 550 to 551
(Cost is: 4549.877917527623) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276 to 552
(Cost is: 5101.877917527623) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276 to 552 to 553
(Cost is: 4836.431747452474) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276 to 277 to 554
(Cost is: 5390.431747452474) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 276 to 277 to 554 to 555
(Cost is: 4718.547219747164) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278 to 556
(Cost is: 5274.547219747164) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278 to 556 to 557
(Cost is: 5007.111447530372) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278 to 279 to 558
(Cost is: 5565.111447530372) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 68 to 69 to 138 to 139 to 278 to 279 to 558 to 559
(Cost is: 4593.360452197363) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280 to 560
(Cost is: 5153.360452197363) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280 to 560 to 561
(Cost is: 4883.935003434854) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280 to 281 to 562
(Cost is: 5445.935003434854) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 280 to 281 to 562 to 563
(Cost is: 4764.091807145167) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282 to 564
(Cost is: 5328.091807145167) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282 to 564 to 565
(Cost is: 5056.67660849013) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282 to 283 to 566
(Cost is: 5622.67660849013) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 140 to 141 to 282 to 283 to 566 to 567
(Cost is: 4733.436092288157) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284 to 568
(Cost is: 5301.436092288157) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284 to 568 to 569
(Cost is: 5028.031071428677) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284 to 285 to 570
(Cost is: 5598.031071428677) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 284 to 285 to 570 to 571
(Cost is: 4906.228622857091) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286 to 572
(Cost is: 5478.228622857091) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286 to 572 to 573
(Cost is: 5202.833708494394) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286 to 287 to 574
(Cost is: 5776.833708494394) 0 to 1 to 2 to 4 to 8 to 16 to 17 to 34 to 35 to 70 to 71 to 142 to 143 to 286 to 287 to 574 to 575
(Cost is: 4711.347475817791) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288 to 576
(Cost is: 5287.347475817791) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288 to 576 to 577
(Cost is: 5009.962597645102) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288 to 289 to 578
(Cost is: 5587.962597645102) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 288 to 289 to 578 to 579
(Cost is: 4886.200329351208) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290 to 580
(Cost is: 5466.200329351208) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290 to 580 to 581
(Cost is: 5186.825418033188) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290 to 291 to 582
(Cost is: 5768.825418033188) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 144 to 145 to 290 to 291 to 582 to 583
(Cost is: 4853.705624678554) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292 to 584
(Cost is: 5437.705624678554) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292 to 584 to 585
(Cost is: 5156.340611831309) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292 to 293 to 586
(Cost is: 5742.340611831309) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 292 to 293 to 586 to 587
(Cost is: 5030.617971961943) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294 to 588
(Cost is: 5618.617971961943) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294 to 588 to 589
(Cost is: 5335.262790133567) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294 to 295 to 590
(Cost is: 5925.262790133567) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 72 to 73 to 146 to 147 to 294 to 295 to 590 to 591
(Cost is: 4895.8345929630095) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296 to 592
(Cost is: 5487.8345929630095) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296 to 592 to 593
(Cost is: 5202.489175614668) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296 to 297 to 594
(Cost is: 5796.489175614668) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 296 to 297 to 594 to 595
(Cost is: 5074.805627270342) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298 to 596
(Cost is: 5670.805627270342) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298 to 596 to 597
(Cost is: 5383.469908757852) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298 to 299 to 598
(Cost is: 5981.469908757852) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 148 to 149 to 298 to 299 to 598 to 599
(Cost is: 5040.4675620775515) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300 to 600
(Cost is: 5640.4675620775515) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300 to 600 to 601
(Cost is: 5351.141477633457) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300 to 301 to 602
(Cost is: 5953.141477633457) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 300 to 301 to 602 to 603
(Cost is: 5221.496498268665) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302 to 604
(Cost is: 5825.496498268665) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302 to 604 to 605
(Cost is: 5534.179983984795) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302 to 303 to 606
(Cost is: 6140.179983984795) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 36 to 37 to 74 to 75 to 150 to 151 to 302 to 303 to 606 to 607
(Cost is: 5033.558630431253) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304 to 608
(Cost is: 5641.558630431253) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304 to 608 to 609
(Cost is: 5348.251623241729) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304 to 305 to 610
(Cost is: 5958.251623241729) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 304 to 305 to 610 to 611
(Cost is: 5216.644704096905) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306 to 612
(Cost is: 5828.644704096905) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306 to 612 to 613
(Cost is: 5533.347141761598) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306 to 307 to 614
(Cost is: 6147.347141761598) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 152 to 153 to 306 to 307 to 614 to 615
(Cost is: 5180.459138713817) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308 to 616
(Cost is: 5796.459138713817) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308 to 616 to 617
(Cost is: 5499.170959802239) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308 to 309 to 618
(Cost is: 6117.170959802239) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 308 to 309 to 618 to 619
(Cost is: 5365.601605365294) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310 to 620
(Cost is: 5985.601605365294) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310 to 620 to 621
(Cost is: 5686.322749240897) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310 to 311 to 622
(Cost is: 6308.322749240897) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 76 to 77 to 154 to 155 to 310 to 311 to 622 to 623
(Cost is: 5221.200245802825) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312 to 624
(Cost is: 5845.200245802825) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312 to 624 to 625
(Cost is: 5543.930652607713) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312 to 313 to 626
(Cost is: 6169.930652607713) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 312 to 313 to 626 to 627
(Cost is: 5408.398380103248) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314 to 628
(Cost is: 6036.398380103248) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314 to 628 to 629
(Cost is: 5733.13799074327) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314 to 315 to 630
(Cost is: 6363.13799074327) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 156 to 157 to 314 to 315 to 630 to 631
(Cost is: 5370.361388045974) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316 to 632
(Cost is: 6002.361388045974) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316 to 632 to 633
(Cost is: 5697.110144176202) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316 to 317 to 634
(Cost is: 6331.110144176202) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 316 to 317 to 634 to 635
(Cost is: 5559.614483080665) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318 to 636
(Cost is: 6195.614483080665) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318 to 636 to 637
(Cost is: 5888.372327091233) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318 to 319 to 638
(Cost is: 6526.372327091233) 0 to 1 to 2 to 4 to 8 to 9 to 18 to 19 to 38 to 39 to 78 to 79 to 158 to 159 to 318 to 319 to 638 to 639
(Cost is: 5328.424340253475) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320 to 640
(Cost is: 5968.424340253475) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320 to 640 to 641
(Cost is: 5659.191215255778) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320 to 321 to 642
(Cost is: 6301.191215255778) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 320 to 321 to 642 to 643
(Cost is: 5519.731706836901) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322 to 644
(Cost is: 6163.731706836901) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322 to 644 to 645
(Cost is: 5852.507556650133) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322 to 323 to 646
(Cost is: 6498.507556650133) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 160 to 161 to 322 to 323 to 646 to 647
(Cost is: 5479.8395587521345) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324 to 648
(Cost is: 6127.8395587521345) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324 to 648 to 649
(Cost is: 5814.624327890157) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324 to 325 to 650
(Cost is: 6464.624327890157) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 324 to 325 to 650 to 651
(Cost is: 5673.200524769203) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326 to 652
(Cost is: 6325.200524769203) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326 to 652 to 653
(Cost is: 6009.994158427746) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326 to 327 to 654
(Cost is: 6663.994158427746) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 80 to 81 to 162 to 163 to 326 to 327 to 654 to 655
(Cost is: 5519.161966161179) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328 to 656
(Cost is: 6175.161966161179) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328 to 656 to 657
(Cost is: 5857.964410205343) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328 to 329 to 658
(Cost is: 6515.964410205343) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 328 to 329 to 658 to 659
(Cost is: 5714.575875940762) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330 to 660
(Cost is: 6374.575875940762) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330 to 660 to 661
(Cost is: 6055.387076892831) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330 to 331 to 662
(Cost is: 6717.387076892831) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 164 to 165 to 330 to 331 to 662 to 663
(Cost is: 5672.825025122962) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332 to 664
(Cost is: 6336.825025122962) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332 to 664 to 665
(Cost is: 6015.6449301505145) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332 to 333 to 666
(Cost is: 6681.6449301505145) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 332 to 333 to 666 to 667
(Cost is: 5870.291238841694) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334 to 668
(Cost is: 6538.291238841694) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334 to 668 to 669
(Cost is: 6215.119795745981) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334 to 335 to 670
(Cost is: 6885.119795745981) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 40 to 41 to 82 to 83 to 166 to 167 to 334 to 335 to 670 to 671
(Cost is: 5657.227563652284) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336 to 672
(Cost is: 6329.227563652284) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336 to 672 to 673
(Cost is: 6004.064720856906) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336 to 337 to 674
(Cost is: 6678.064720856906) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 336 to 337 to 674 to 675
(Cost is: 5856.745456766856) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338 to 676
(Cost is: 6532.745456766856) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338 to 676 to 677
(Cost is: 6205.591163306698) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338 to 339 to 678
(Cost is: 6883.591163306698) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 168 to 169 to 338 to 339 to 678 to 679
(Cost is: 5813.132526060305) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340 to 680
(Cost is: 6493.132526060305) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340 to 680 to 681
(Cost is: 6163.986731570729) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340 to 341 to 682
(Cost is: 6845.986731570729) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 340 to 341 to 682 to 683
(Cost is: 6014.701488766543) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342 to 684
(Cost is: 6698.701488766543) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342 to 684 to 685
(Cost is: 6367.56414347284) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342 to 343 to 686
(Cost is: 7053.56414347284) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 84 to 85 to 170 to 171 to 342 to 343 to 686 to 687
(Cost is: 5851.008354084517) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344 to 688
(Cost is: 6539.008354084517) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344 to 688 to 689
(Cost is: 6205.879408791604) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344 to 345 to 690
(Cost is: 6895.879408791604) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 344 to 345 to 690 to 691
(Cost is: 6054.6277908016955) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346 to 692
(Cost is: 6746.6277908016955) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346 to 692 to 693
(Cost is: 6411.507196884045) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346 to 347 to 694
(Cost is: 7105.507196884045) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 172 to 173 to 346 to 347 to 694 to 695
(Cost is: 6009.149560725731) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348 to 696
(Cost is: 6705.149560725731) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348 to 696 to 697
(Cost is: 6368.037270117542) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348 to 349 to 698
(Cost is: 7066.037270117542) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 348 to 349 to 698 to 699
(Cost is: 6214.818889604667) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350 to 700
(Cost is: 6914.818889604667) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350 to 700 to 701
(Cost is: 6575.7148547902525) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350 to 351 to 702
(Cost is: 7277.7148547902525) 0 to 1 to 2 to 4 to 5 to 10 to 20 to 21 to 42 to 43 to 86 to 87 to 174 to 175 to 350 to 351 to 702 to 703
(Cost is: 5963.995752190083) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352 to 704
(Cost is: 6667.995752190083) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352 to 704 to 705
(Cost is: 6326.899926194482) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352 to 353 to 706
(Cost is: 7032.899926194482) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 352 to 353 to 706 to 707
(Cost is: 6171.714404643662) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354 to 708
(Cost is: 6879.714404643662) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354 to 708 to 709
(Cost is: 6536.626741023458) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354 to 355 to 710
(Cost is: 7246.626741023458) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 176 to 177 to 354 to 355 to 710 to 711
(Cost is: 6124.367802601575) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356 to 712
(Cost is: 6836.367802601575) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356 to 712 to 713
(Cost is: 6491.288255435926) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356 to 357 to 714
(Cost is: 7205.288255435926) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 356 to 357 to 714 to 715
(Cost is: 6334.135222856424) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358 to 716
(Cost is: 7050.135222856424) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358 to 716 to 717
(Cost is: 6703.063746738295) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358 to 359 to 718
(Cost is: 7421.063746738295) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 88 to 89 to 178 to 179 to 358 to 359 to 718 to 719
(Cost is: 6160.771723911998) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360 to 720
(Cost is: 6880.771723911998) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360 to 720 to 721
(Cost is: 6531.708273939584) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360 to 361 to 722
(Cost is: 7253.708273939584) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 360 to 361 to 722 to 723
(Cost is: 6372.587368580154) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362 to 724
(Cost is: 7096.587368580154) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362 to 724 to 725
(Cost is: 6745.531900348492) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362 to 363 to 726
(Cost is: 7471.531900348492) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 180 to 181 to 362 to 363 to 726 to 727
(Cost is: 6323.369459030872) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364 to 728
(Cost is: 7051.369459030872) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364 to 728 to 729
(Cost is: 6698.32192862364) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364 to 365 to 730
(Cost is: 7428.32192862364) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 364 to 365 to 730 to 731
(Cost is: 6537.232796700324) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366 to 732
(Cost is: 7269.232796700324) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366 to 732 to 733
(Cost is: 6914.193160681823) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366 to 367 to 734
(Cost is: 7648.193160681823) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 44 to 45 to 90 to 91 to 182 to 183 to 366 to 367 to 734 to 735
(Cost is: 6298.963062948098) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368 to 736
(Cost is: 7034.963062948098) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368 to 736 to 737
(Cost is: 6677.931278355402) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368 to 369 to 738
(Cost is: 7415.931278355402) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 368 to 369 to 738 to 739
(Cost is: 6514.873573791179) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370 to 740
(Cost is: 7254.873573791179) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370 to 740 to 741
(Cost is: 6895.849598126461) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370 to 371 to 742
(Cost is: 7637.849598126461) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 184 to 185 to 370 to 371 to 742 to 743
(Cost is: 6463.7815492787095) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372 to 744
(Cost is: 7207.7815492787095) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372 to 744 to 745
(Cost is: 6846.765340501726) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372 to 373 to 746
(Cost is: 7592.765340501726) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 372 to 373 to 746 to 747
(Cost is: 6681.738724677392) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374 to 748
(Cost is: 7429.738724677392) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374 to 748 to 749
(Cost is: 7066.730241198133) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374 to 375 to 750
(Cost is: 7816.730241198133) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 92 to 93 to 186 to 187 to 374 to 375 to 750 to 751
(Cost is: 6498.6904537014925) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376 to 752
(Cost is: 7250.6904537014925) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376 to 752 to 753
(Cost is: 6885.689654372987) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376 to 377 to 754
(Cost is: 7639.689654372987) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 376 to 377 to 754 to 755
(Cost is: 6718.693795888654) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378 to 756
(Cost is: 7474.693795888654) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378 to 756 to 757
(Cost is: 7107.700639999932) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378 to 379 to 758
(Cost is: 7865.700639999932) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 188 to 189 to 378 to 379 to 758 to 759
(Cost is: 6665.724968837688) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380 to 760
(Cost is: 7425.724968837688) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380 to 760 to 761
(Cost is: 7056.739416106883) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380 to 381 to 762
(Cost is: 7818.739416106883) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 380 to 381 to 762 to 763
(Cost is: 6887.773990553532) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382 to 764
(Cost is: 7651.773990553532) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382 to 764 to 765
(Cost is: 7280.796001121134) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382 to 383 to 766
(Cost is: 8046.796001121134) 0 to 1 to 2 to 4 to 5 to 10 to 11 to 22 to 23 to 46 to 47 to 94 to 95 to 190 to 191 to 382 to 383 to 766 to 767
(Cost is: 6594.496313051684) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384 to 768
(Cost is: 7362.496313051684) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384 to 768 to 769
(Cost is: 6989.525847473933) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384 to 385 to 770
(Cost is: 7759.525847473933) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 384 to 385 to 770 to 771
(Cost is: 6818.590537214517) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386 to 772
(Cost is: 7590.590537214517) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386 to 772 to 773
(Cost is: 7215.627556456924) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386 to 387 to 774
(Cost is: 7989.627556456924) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 192 to 193 to 386 to 387 to 774 to 775
(Cost is: 6763.742332412128) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388 to 776
(Cost is: 7539.742332412128) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388 to 776 to 777
(Cost is: 7162.7867978431495) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388 to 389 to 778
(Cost is: 7940.7867978431495) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 388 to 389 to 778 to 779
(Cost is: 6989.8812918027415) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390 to 780
(Cost is: 7769.8812918027415) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390 to 780 to 781
(Cost is: 7390.933165187568) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390 to 391 to 782
(Cost is: 8172.933165187568) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 96 to 97 to 194 to 195 to 390 to 391 to 782 to 783
(Cost is: 6797.13504795713) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392 to 784
(Cost is: 7581.13504795713) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392 to 784 to 785
(Cost is: 7200.1942914516) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392 to 393 to 786
(Cost is: 7986.1942914516) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 392 to 393 to 786 to 787
(Cost is: 7025.318284916107) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394 to 788
(Cost is: 7813.318284916107) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394 to 788 to 789
(Cost is: 7430.384861060758) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394 to 395 to 790
(Cost is: 8220.384861060758) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 196 to 197 to 394 to 395 to 790 to 791
(Cost is: 6968.588232609267) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396 to 792
(Cost is: 7760.588232609267) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396 to 792 to 793
(Cost is: 7375.662104323496) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396 to 397 to 794
(Cost is: 8169.662104323496) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 396 to 397 to 794 to 795
(Cost is: 7198.815298746537) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398 to 796
(Cost is: 7994.815298746537) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398 to 796 to 797
(Cost is: 7607.896429322882) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398 to 399 to 798
(Cost is: 8405.896429322882) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 48 to 49 to 98 to 99 to 198 to 199 to 398 to 399 to 798 to 799
(Cost is: 6935.272759590556) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400 to 800
(Cost is: 7735.272759590556) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400 to 800 to 801
(Cost is: 7346.361112689089) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400 to 401 to 802
(Cost is: 8148.361112689089) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 400 to 401 to 802 to 803
(Cost is: 7167.5432155066155) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402 to 804
(Cost is: 7971.5432155066155) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402 to 804 to 805
(Cost is: 7580.638755149457) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402 to 403 to 806
(Cost is: 8386.638755149457) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 200 to 201 to 402 to 403 to 806 to 807
(Cost is: 7108.928945053767) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404 to 808
(Cost is: 7916.928945053767) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404 to 808 to 809
(Cost is: 7524.031635619696) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404 to 405 to 810
(Cost is: 8334.031635619696) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 404 to 405 to 810 to 811
(Cost is: 7343.242360071952) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406 to 812
(Cost is: 8155.242360071952) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406 to 812 to 813
(Cost is: 7760.3521662911335) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406 to 407 to 814
(Cost is: 8574.352166291133) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 100 to 101 to 202 to 203 to 406 to 407 to 814 to 815
(Cost is: 7140.786003867706) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408 to 816
(Cost is: 7956.786003867706) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408 to 816 to 817
(Cost is: 7559.902890816512) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408 to 409 to 818
(Cost is: 8377.902890816513) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 408 to 409 to 818 to 819
(Cost is: 7377.141955777001) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410 to 820
(Cost is: 8197.141955777002) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410 to 820 to 821
(Cost is: 7798.26588887294) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410 to 411 to 822
(Cost is: 8620.26588887294) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 204 to 205 to 410 to 411 to 822 to 823
(Cost is: 7316.641189799158) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412 to 824
(Cost is: 8140.641189799158) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412 to 824 to 825
(Cost is: 7739.772134795904) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412 to 413 to 826
(Cost is: 8565.772134795905) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 412 to 413 to 826 to 827
(Cost is: 7555.039264607011) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414 to 828
(Cost is: 8383.03926460701) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414 to 828 to 829
(Cost is: 7980.177187589519) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414 to 415 to 830
(Cost is: 8810.17718758952) 0 to 1 to 2 to 3 to 6 to 12 to 24 to 25 to 50 to 51 to 102 to 103 to 206 to 207 to 414 to 415 to 830 to 831
(Cost is: 7247.924791664045) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416 to 832
(Cost is: 8079.924791664045) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416 to 832 to 833
(Cost is: 7675.069659043776) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416 to 417 to 834
(Cost is: 8509.069659043776) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 416 to 417 to 834 to 835
(Cost is: 7488.364583358717) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418 to 836
(Cost is: 8324.364583358718) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418 to 836 to 837
(Cost is: 7917.516361868936) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418 to 419 to 838
(Cost is: 8755.516361868937) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 208 to 209 to 418 to 419 to 838 to 839
(Cost is: 7425.97513234193) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420 to 840
(Cost is: 8265.97513234193) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420 to 840 to 841
(Cost is: 7857.133789033111) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420 to 421 to 842
(Cost is: 8699.13378903311) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 420 to 421 to 842 to 843
(Cost is: 7668.456242663921) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422 to 844
(Cost is: 8512.45624266392) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422 to 844 to 845
(Cost is: 8101.621744899227) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422 to 423 to 846
(Cost is: 8947.621744899228) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 104 to 105 to 210 to 211 to 422 to 423 to 846 to 847
(Cost is: 7456.278572891737) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424 to 848
(Cost is: 8304.278572891737) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424 to 848 to 849
(Cost is: 7891.450888342593) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424 to 425 to 850
(Cost is: 8741.450888342593) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 424 to 425 to 850 to 851
(Cost is: 7700.800611113853) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426 to 852
(Cost is: 8552.800611113853) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426 to 852 to 853
(Cost is: 8137.979707755595) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426 to 427 to 854
(Cost is: 8991.979707755596) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 212 to 213 to 426 to 427 to 854 to 855
(Cost is: 7636.520368420301) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428 to 856
(Cost is: 8492.5203684203) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428 to 856 to 857
(Cost is: 8075.706214527915) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428 to 429 to 858
(Cost is: 8933.706214527916) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 428 to 429 to 858 to 859
(Cost is: 7883.082951135966) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430 to 860
(Cost is: 8743.082951135966) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430 to 860 to 861
(Cost is: 8324.2755152799) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430 to 431 to 862
(Cost is: 9186.2755152799) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 52 to 53 to 106 to 107 to 214 to 215 to 430 to 431 to 862 to 863
(Cost is: 7594.211218153149) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432 to 864
(Cost is: 8458.211218153148) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432 to 864 to 865
(Cost is: 8037.410469195202) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432 to 433 to 866
(Cost is: 8903.410469195202) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 432 to 433 to 866 to 867
(Cost is: 7842.813969072577) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434 to 868
(Cost is: 8710.813969072577) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434 to 868 to 869
(Cost is: 8288.019876161872) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434 to 435 to 870
(Cost is: 9158.019876161872) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 216 to 217 to 434 to 435 to 870 to 871
(Cost is: 7776.64090632236) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436 to 872
(Cost is: 8648.64090632236) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436 to 872 to 873
(Cost is: 8223.853438891383) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436 to 437 to 874
(Cost is: 9097.853438891383) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 436 to 437 to 874 to 875
(Cost is: 8027.283456076222) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438 to 876
(Cost is: 8903.283456076222) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438 to 876 to 877
(Cost is: 8476.50258383693) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438 to 439 to 878
(Cost is: 9354.50258383693) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 108 to 109 to 218 to 219 to 438 to 439 to 878 to 879
(Cost is: 7805.374105058583) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440 to 880
(Cost is: 8685.374105058583) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440 to 880 to 881
(Cost is: 8256.599797998599) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440 to 441 to 882
(Cost is: 9138.599797998599) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 440 to 441 to 882 to 883
(Cost is: 8058.056091008827) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442 to 884
(Cost is: 8942.056091008828) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442 to 884 to 885
(Cost is: 8511.288319387688) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442 to 443 to 886
(Cost is: 9397.288319387688) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 220 to 221 to 442 to 443 to 886 to 887
(Cost is: 7989.988253798627) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444 to 888
(Cost is: 8877.988253798627) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444 to 888 to 889
(Cost is: 8445.226988144103) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444 to 445 to 890
(Cost is: 9335.226988144103) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 444 to 445 to 890 to 891
(Cost is: 8244.70931985614) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446 to 892
(Cost is: 9136.70931985614) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446 to 892 to 893
(Cost is: 8701.954530960627) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446 to 447 to 894
(Cost is: 9595.954530960627) 0 to 1 to 2 to 3 to 6 to 12 to 13 to 26 to 27 to 54 to 55 to 110 to 111 to 222 to 223 to 446 to 447 to 894 to 895
(Cost is: 7895.493413211373) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448 to 896
(Cost is: 8791.493413211374) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448 to 896 to 897
(Cost is: 8354.745072128342) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448 to 449 to 898
(Cost is: 9252.745072128342) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 448 to 449 to 898 to 899
(Cost is: 8152.2532096601635) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450 to 900
(Cost is: 9052.253209660164) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450 to 900 to 901
(Cost is: 8613.511287700674) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450 to 451 to 902
(Cost is: 9515.511287700674) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 224 to 225 to 450 to 451 to 902 to 903
(Cost is: 8082.288713588626) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452 to 904
(Cost is: 8986.288713588627) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452 to 904 to 905
(Cost is: 8545.553182317904) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452 to 453 to 906
(Cost is: 9451.553182317904) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 452 to 453 to 906 to 907
(Cost is: 8341.086896916242) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454 to 908
(Cost is: 9249.086896916242) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454 to 908 to 909
(Cost is: 8806.357728150322) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454 to 455 to 910
(Cost is: 9716.357728150322) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 112 to 113 to 226 to 227 to 454 to 455 to 910 to 911
(Cost is: 8109.436240793841) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456 to 912
(Cost is: 9021.43624079384) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456 to 912 to 913
(Cost is: 8576.713406596256) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456 to 457 to 914
(Cost is: 9490.713406596256) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 456 to 457 to 914 to 915
(Cost is: 8370.272473527759) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458 to 916
(Cost is: 9286.272473527759) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458 to 916 to 917
(Cost is: 8839.555946206296) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458 to 459 to 918
(Cost is: 9757.555946206296) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 228 to 229 to 458 to 459 to 918 to 919
(Cost is: 8298.409500500598) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460 to 920
(Cost is: 9218.409500500598) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460 to 920 to 921
(Cost is: 8769.69925260411) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460 to 461 to 922
(Cost is: 9691.69925260411) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 460 to 461 to 922 to 923
(Cost is: 8561.283451050307) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462 to 924
(Cost is: 9485.283451050307) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462 to 924 to 925
(Cost is: 9034.57945536558) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462 to 463 to 926
(Cost is: 9960.57945536558) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 56 to 57 to 114 to 115 to 230 to 231 to 462 to 463 to 926 to 927
(Cost is: 8247.034255485061) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464 to 928
(Cost is: 9175.034255485061) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464 to 928 to 929
(Cost is: 8722.336485033724) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464 to 465 to 930
(Cost is: 9652.336485033724) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 464 to 465 to 930 to 931
(Cost is: 8511.945597989612) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466 to 932
(Cost is: 9443.945597989612) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466 to 932 to 933
(Cost is: 8989.25402602513) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466 to 467 to 934
(Cost is: 9923.25402602513) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 232 to 233 to 466 to 467 to 934 to 935
(Cost is: 8438.18239278028) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468 to 936
(Cost is: 9374.18239278028) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468 to 936 to 937
(Cost is: 8917.49699278496) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468 to 469 to 938
(Cost is: 9855.49699278496) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 468 to 469 to 938 to 939
(Cost is: 8705.130806961055) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470 to 940
(Cost is: 9645.130806961055) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470 to 940 to 941
(Cost is: 9186.451552643128) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470 to 471 to 942
(Cost is: 10128.451552643128) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 116 to 117 to 234 to 235 to 470 to 471 to 942 to 943
(Cost is: 8463.729888409303) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472 to 944
(Cost is: 9407.729888409303) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472 to 944 to 945
(Cost is: 8947.056753700046) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472 to 473 to 946
(Cost is: 9893.056753700046) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 472 to 473 to 946 to 947
(Cost is: 8732.715059427781) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474 to 948
(Cost is: 9680.715059427781) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474 to 948 to 949
(Cost is: 9218.048018478705) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474 to 475 to 950
(Cost is: 10168.048018478705) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 236 to 237 to 474 to 475 to 950 to 951
(Cost is: 8657.049926471938) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476 to 952
(Cost is: 9609.049926471938) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476 to 952 to 953
(Cost is: 9144.388953651986) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476 to 477 to 954
(Cost is: 10098.388953651986) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 476 to 477 to 954 to 955
(Cost is: 8928.071544792263) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478 to 956
(Cost is: 9884.071544792263) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478 to 956 to 957
(Cost is: 9417.416614685095) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478 to 479 to 958
(Cost is: 10375.416614685095) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 28 to 29 to 58 to 59 to 118 to 119 to 238 to 239 to 478 to 479 to 958 to 959
(Cost is: 8564.417984806616) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480 to 960
(Cost is: 9524.417984806616) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480 to 960 to 961
(Cost is: 9055.769072207917) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480 to 481 to 962
(Cost is: 10017.769072207917) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 480 to 481 to 962 to 963
(Cost is: 8837.475746062744) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482 to 964
(Cost is: 9801.475746062744) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482 to 964 to 965
(Cost is: 9330.83282597758) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482 to 483 to 966
(Cost is: 10296.83282597758) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 240 to 241 to 482 to 483 to 966 to 967
(Cost is: 8759.907046467044) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484 to 968
(Cost is: 9727.907046467044) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484 to 968 to 969
(Cost is: 9255.270094107267) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484 to 485 to 970
(Cost is: 10225.270094107267) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 484 to 485 to 970 to 971
(Cost is: 9035.000651334301) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486 to 972
(Cost is: 10007.000651334301) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486 to 972 to 973
(Cost is: 9532.369642115993) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486 to 487 to 974
(Cost is: 10506.369642115993) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 120 to 121 to 242 to 243 to 486 to 487 to 974 to 975
(Cost is: 8783.841112628992) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488 to 976
(Cost is: 9759.841112628992) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488 to 976 to 977
(Cost is: 9283.216022169945) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488 to 489 to 978
(Cost is: 10261.216022169945) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 488 to 489 to 978 to 979
(Cost is: 9060.97026669985) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490 to 980
(Cost is: 10040.97026669985) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490 to 980 to 981
(Cost is: 9562.351070817103) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490 to 491 to 982
(Cost is: 10544.351070817103) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 244 to 245 to 490 to 491 to 982 to 983
(Cost is: 8981.496415447371) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492 to 984
(Cost is: 9965.496415447371) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492 to 984 to 985
(Cost is: 9484.883090154777) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492 to 493 to 986
(Cost is: 10470.883090154777) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 492 to 493 to 986 to 987
(Cost is: 9260.660829111257) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494 to 988
(Cost is: 10248.660829111257) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494 to 988 to 989
(Cost is: 9766.05335061708) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494 to 495 to 990
(Cost is: 10756.05335061708) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 60 to 61 to 122 to 123 to 246 to 247 to 494 to 495 to 990 to 991
(Cost is: 8920.992385172483) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496 to 992
(Cost is: 9912.992385172483) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496 to 992 to 993
(Cost is: 9428.39072987705) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496 to 497 to 994
(Cost is: 10422.39072987705) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 496 to 497 to 994 to 995
(Cost is: 9202.191773499504) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498 to 996
(Cost is: 10198.191773499504) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498 to 996 to 997
(Cost is: 9711.595917992892) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498 to 499 to 998
(Cost is: 10709.595917992892) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 248 to 249 to 498 to 499 to 998 to 999
(Cost is: 9120.811236836002) 0 to 1 to 2 to 3 to 6 to 7 to 14 to 15 to 30 to 31 to 62 to 124 to 125 to 250 to 500 to 1000


*/