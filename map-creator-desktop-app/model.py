class Point:
    def __init__(self, x: float, y: float):
        self.x = x
        self.y = y

    def as_tuple(self):
        return self.x, self.y


class Wall:
    def __init__(self, start: Point, end: Point):
        self.start = start
        self.end = end


class BuildingMap:
    def __init__(self):
        self.walls = []

    def add_wall(self, start: Point, end: Point):
        self.walls.append(Wall(start, end))

    def get_points(self):
        points = []
        for w in self.walls:
            points.append(w.start)
            points.append(w.end)
        return points