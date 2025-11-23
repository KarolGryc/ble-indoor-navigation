from PySide6.QtCore import QObject
from node import Node
from wall import Wall
from PySide6.QtCore import Signal

class MapModel(QObject):
    node_added = Signal(Node)
    node_removed = Signal(Node)

    wall_added = Signal(Wall)
    wall_removed = Signal(Wall)

    def __init__(self):
        super().__init__()
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []

    def add_node(self, node: Node):
        self.nodes.append(node)
        self.node_added.emit(node)

    def remove_node(self, node: Node):
        if node in self.nodes:
            self.nodes.remove(node)
            self.node_removed.emit(node)

    def add_wall(self, wall: Wall):
        self.walls.append(wall)
        self.wall_added.emit(wall)

    def remove_wall(self, wall: Wall):
        if wall in self.walls:
            self.walls.remove(wall)
            self.wall_removed.emit(wall)