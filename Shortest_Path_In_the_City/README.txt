
Route Optimization and Visualization Tool using OSMnx and A* / Dijkstra’s Algorithms

Description:
This project showcases a geospatial routing application that combines open-source mapping data, modern geocoding services, and shortest-path algorithms to provide efficient navigation solutions. Leveraging OpenStreetMap data via the OSMnx library, it allows users to dynamically retrieve the road network graph of a given geographic region—specifically, roads in Salt Lake County, Utah. Users simply input start and end locations, and the system handles the rest, from geocoding addresses to plotting the resulting optimal route on a high-resolution map.

Key Features:
	1.	Geocoding with OpenCage:
Input addresses or place names are converted into geographic coordinates using the OpenCage Geocoder API. This ensures accurate, real-world positioning of the start and end points, even if the user does not provide a precise latitude/longitude.
	2.	Customizable Road Network Extraction with OSMnx:
The road network is sourced directly from OpenStreetMap and filtered to include only higher-level roadways (e.g., motorways, primary roads, secondary roads). This reduces unnecessary complexity and improves computation time. The resulting graph captures essential street geometry and attributes (e.g., lengths, intersections) for optimal routing.
	3.	Shortest Path Algorithms (A and Dijkstra):*
The project implements two well-known pathfinding algorithms:
	•	A*: An informed search algorithm that uses a heuristic (geographic distance) to find the shortest path more efficiently.
	•	Dijkstra’s Algorithm: A classic algorithm that systematically explores all paths to find the absolute shortest route, ensuring reliable results even without a heuristic.
	4.	Route Visualization:
After computing the shortest path, the tool visually presents the route superimposed on the street network. Using Matplotlib, the code highlights the chosen path, along with markers for both start and end points. The resulting plot is both visually appealing and highly informative.
	5.	User-Friendly Interaction:
The command-line interface requests the start and end locations by name, making it simple for non-technical users. The entire process—geocoding, routing, and visualization—runs seamlessly within a single script.


Technologies & Libraries Used:
	•	Python 3: Core programming language for data handling and integration.
	•	OSMnx: Used to download and model street networks from OpenStreetMap.
	•	NetworkX: Provides graph structures and fundamental shortest-path algorithms.
	•	Requests: Interfacing with the OpenCage Geocoding API.
	•	Matplotlib: For plotting and visualizing the resulting route.
