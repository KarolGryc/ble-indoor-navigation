from PySide6.QtCore import QObject
from model.node import Node
from model.wall import Wall
from model.zone import Zone
from PySide6.QtCore import Signal

class MapModel(QObject):
    node_added = Signal(Node)

    node_removed = Signal(Node)

    wall_added = Signal(Wall)
    wall_removed = Signal(Wall)

    zone_added = Signal(Zone)
    zone_removed = Signal(Zone)

    def __init__(self):
        super().__init__()
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []
        self.zones: list[Zone] = []

    def add_node(self, node: Node):
        if node in self.nodes:
            return
        
        self.nodes.append(node)
        self.node_added.emit(node)

    def remove_node(self, node: Node):
        if node in self.nodes:
            self.nodes.remove(node)
            self.node_removed.emit(node)

    def add_wall(self, wall: Wall):
        if wall in self.walls:
            return
        
        self.walls.append(wall)
        self.wall_added.emit(wall)

    def remove_wall(self, wall: Wall):
        if wall in self.walls:
            self.walls.remove(wall)
            self.wall_removed.emit(wall)

    def add_zone(self, zone: Zone):
        if zone in self.zones:
            return
        
        self.zones.append(zone)
        self.zone_added.emit(zone)

    def remove_zone(self, zone: Zone):
        if zone in self.zones:
            self.zones.remove(zone)
            self.zone_removed.emit(zone)
