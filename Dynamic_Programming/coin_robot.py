import random 
from draw import draw
import argparse

class coin_robot:

    def __init__(self, row, column):
        random.seed(0)
        self.row = row
        self.column = column 
        # Get map
        self.map = [[0 for i in range(column)] for j in range(row)]
        self.generate_map()
        
    def generate_map(self):
        for i in range(self.row):
            for j in range(self.column):
                if random.random() > 0.7:
                    self.map[i][j] = 1 # coin
                else:
                    self.map[i][j] = 0

    def solve(self):
        # Initialize 
        F = []
        for i in range(self.row):
            row = []
            for j in range(self.column):
                row.append(0)
            F.append(row)
        parent = {}

        #print(parent)
        #print(F)
        #print to check the initialization

        # Fill the table
        for i in range(self.row):
            for j in range(self.column):
                # case for (0, 0)
                if i == 0 and j == 0:
                    F[i][j] = self.map[i][j]
                    parent[(i, j)] = None
                #only comes from the left:
                elif i == 0:
                    F[i][j] = F[i][j - 1] + self.map[i][j]
                    parent[(i, j)] = (i, j - 1)
                # only comes from above:
                elif j == 0:
                    F[i][j] = F[i - 1][j] + self.map[i][j]
                    parent[(i, j)] = (i - 1, j)
                # from top or left:
                else:
                    if F[i - 1][j] > F[i][j - 1]:
                        F[i][j] = F[i - 1][j] + self.map[i][j]
                        parent[(i, j)] = (i - 1, j)
                    else:
                        F[i][j] = F[i][j - 1] + self.map[i][j]
                        parent[(i, j)] = (i, j - 1)

        # backing
        path = []
        current = (self.row - 1, self.column - 1)
        while current is not None:
            path.append(current)
            current = parent[current]
        path.reverse() #reverse can make it the right order


        F_value = F[self.row - 1][self.column - 1]
        self.draw(F_value, path)

    def draw(self, F, path):
        title = "row_"+str(self.row)+"_column_"+str(self.column)+"_value_"+str(F)
        draw(self.map, path, title)

if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='coin robot')

    parser.add_argument('-row', dest='row', required = True, type = int, help='number of row')
    parser.add_argument('-column', dest='column', required = True, type = int, help='number of column')

    args = parser.parse_args()

    # Example: 
    # python coin_robot.py -row 20 -column 20
    game = coin_robot(args.row, args.column)
    game.solve()