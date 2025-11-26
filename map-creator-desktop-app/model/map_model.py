from PySide6.QtCore import QObject
from model.node import Node
from model.wall import Wall
from model.zone import Zone
from model.point_of_interest import PointOfInterest
from PySide6.QtCore import Signal

from model.map_object import MapObject

class MapModel(QObject):
    item_added = Signal(MapObject)
    item_removed = Signal(MapObject)

    def __init__(self):
        super().__init__()
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []
        self.zones: list[Zone] = []
        self.points_of_interest: list[PointOfInterest] = []

    def add_node(self, node: Node):
        if node in self.nodes:
            return
        
        self.nodes.append(node)
        self.item_added.emit(node)

    def remove_node(self, node: Node):
        if node in self.nodes:
            self.nodes.remove(node)
            self.item_removed.emit(node)

    def add_wall(self, wall: Wall):
        if wall in self.walls:
            return
        
        self.walls.append(wall)
        self.item_added.emit(wall)

    def remove_wall(self, wall: Wall):
        if wall in self.walls:
            self.walls.remove(wall)
            self.item_removed.emit(wall)

    def add_zone(self, zone: Zone):
        if zone in self.zones:
            return
        
        self.zones.append(zone)
        self.item_added.emit(zone)

    def remove_zone(self, zone: Zone):
        if zone in self.zones:
            self.zones.remove(zone)
            self.item_removed.emit(zone)

    def add_point_of_interest(self, poi: PointOfInterest):
        if poi in self.points_of_interest:
            return
        
        self.points_of_interest.append(poi)
        self.item_added.emit(poi)

    def remove_point_of_interest(self, poi: PointOfInterest):
        if poi in self.points_of_interest:
            self.points_of_interest.remove(poi)
            self.item_removed.emit(poi)