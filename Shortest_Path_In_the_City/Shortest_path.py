import osmnx as ox
import networkx as nx
import matplotlib.pyplot as plt
import heapq
import requests

# OpenCage Geocoder API
def get_coordinates(place_name, api_key):

    base_url = "https://api.opencagedata.com/geocode/v1/json"
    params = {
        "q": place_name,
        "key": api_key,
        "limit": 1  # Return top result
    }
    response = requests.get(base_url, params=params)
    if response.status_code == 200:
        results = response.json().get("results")
        if results:
            location = results[0]["geometry"]
            return location["lat"], location["lng"]
        else:
            print("No results found for the specified location.")
    else:
        print(f"Error: Received response code {response.status_code}")
    return None

# A*
def astar(graph, start, goal):
    def heuristic(u, v):
        (x1, y1) = graph.nodes[u]["x"], graph.nodes[u]["y"]
        (x2, y2) = graph.nodes[v]["x"], graph.nodes[v]["y"]
        return ((x1 - x2) ** 2 + (y1 - y2) ** 2) ** 0.5

    open_set = []
    heapq.heappush(open_set, (0, start))
    came_from = {}
    g_score = {node: float("inf") for node in graph.nodes}
    g_score[start] = 0
    f_score = {node: float("inf") for node in graph.nodes}
    f_score[start] = heuristic(start, goal)

    while open_set:
        _, current = heapq.heappop(open_set)

        if current == goal:
            path = []
            while current in came_from:
                path.append(current)
                current = came_from[current]
            path.append(start)
            return path[::-1]

        for neighbor in graph.neighbors(current):
            tentative_g_score = g_score[current] + graph.edges[current, neighbor, 0]["length"]
            if tentative_g_score < g_score[neighbor]:
                came_from[neighbor] = current
                g_score[neighbor] = tentative_g_score
                f_score[neighbor] = g_score[neighbor] + heuristic(neighbor, goal)
                if neighbor not in [i[1] for i in open_set]:
                    heapq.heappush(open_set, (f_score[neighbor], neighbor))

    return None

# Dijkstra
def dijkstra(graph, start, goal):
    queue = []
    heapq.heappush(queue, (0, start))
    distances = {node: float("inf") for node in graph.nodes}
    distances[start] = 0
    came_from = {}

    while queue:
        current_distance, current_node = heapq.heappop(queue)

        if current_node == goal:
            path = []
            while current_node in came_from:
                path.append(current_node)
                current_node = came_from[current_node]
            path.append(start)
            return path[::-1]

        for neighbor in graph.neighbors(current_node):
            weight = graph.edges[current_node, neighbor, 0]["length"]
            distance = current_distance + weight

            if distance < distances[neighbor]:
                distances[neighbor] = distance
                came_from[neighbor] = current_node
                heapq.heappush(queue, (distance, neighbor))

    return None

# Main function
def main():
    #  OpenCage API
    api_key = "431baf6f366144239f77f6f1fd9907e5"

    # Fetch data
    counties = ["Salt Lake County, Utah, USA"]
    graphs = []
    for county in counties:
        graph = ox.graph_from_place(
            county,
            network_type="drive",
            custom_filter='["highway"~"motorway|trunk|primary|secondary|tertiary"]'
        )
        graphs.append(graph)

    # excluding living street for lighter load for computer.
    # for more detail map: ["highway"~"motorway|trunk|primary|secondary|tertiary|residential|living_street"]'


    # Ask for a starting location
    start_location = input("Enter the starting location in salt lake county: ")
    start_coords = get_coordinates(start_location, api_key)
    if not start_coords:
        print("Starting location not found. Please try again.")
        return

    # Ask for an ending location
    end_location = input("Enter the ending location in salt lake county:  ")
    end_coords = get_coordinates(end_location, api_key)
    if not end_coords:
        print("Ending location not found. Please try again.")
        return

    # nearest node in ox package (estimation) luckily the package include this
    start_node = ox.distance.nearest_nodes(graph, X=start_coords[1], Y=start_coords[0])
    end_node = ox.distance.nearest_nodes(graph, X=end_coords[1], Y=end_coords[0])

    # select an algorithm
    print("Choose an algorithm to find the shortest path:")
    print("a) A* Algorithm")
    print("b) Dijkstra's Algorithm")
    choice = input("Enter 'a' or 'b': ").strip().lower()

    if choice == "a":
        path = astar(graph, start_node, end_node)
    elif choice == "b":
        path = dijkstra(graph, start_node, end_node)
    else:
        print("Invalid choice. Please enter 'a' or 'b'.")
        return

    if not path:
        print("No path found between the specified locations.")
        return

    #  Plot the shortest path, here are ox plotting parameter, tweaked for best definition.
    fig, ax = ox.plot_graph_route(
        graph, path, route_linewidth=2, node_size=0, figsize=(48, 48), show=False, close=False, dpi = 2000
    )

    # Marks
    ax.scatter(start_coords[1], start_coords[0], color="blue", s=100, label=start_location)
    ax.scatter(end_coords[1], end_coords[0], color="red", s=100, label=end_location)


    ax.legend()
    plt.show()


if __name__ == "__main__":
    main()

    #    #try upland terrace elementary school, parkview elementary school, 300 west town center, orson gygi,