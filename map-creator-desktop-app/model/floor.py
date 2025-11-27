from PySide6.QtCore import QObject
from model.node import Node
from model.wall import Wall
from model.zone import Zone
from model.point_of_interest import PointOfInterest
from PySide6.QtCore import Signal

from model.map_object import MapObject

class Floor(QObject):
    item_added = Signal(MapObject)
    item_removed = Signal(MapObject)
    name_changed = Signal(str)

    def __init__(self, name: str = "Unnamed Floor"):
        super().__init__()
        self._name = name
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []
        self.zones: list[Zone] = []
        self.points_of_interest: list[PointOfInterest] = []

        self._type_to_list = {
            Node: self.nodes,
            Wall: self.walls,
            Zone: self.zones,
            PointOfInterest: self.points_of_interest,
        }

    @property
    def name(self) -> str:
        return self._name
    
    @name.setter
    def name(self, new_name: str):
        self._name = new_name
        self.name_changed.emit(new_name)

    def add(self, element: MapObject):
        el_type = type(element)
        type_list = self._type_to_list.get(el_type, None)
        
        if type_list is not None and element not in type_list:
            for dependency in element.dependencies:
                self.add(dependency)
                
            type_list.append(element)
            self.item_added.emit(element)

    def remove(self, element: MapObject):
        el_type = type(element)
        type_list = self._type_to_list.get(el_type, None)
        
        if type_list is not None and element in type_list:
            type_list.remove(element)
            for dependency in element.dependencies:
                self.remove(dependency)

            self.item_removed.emit(element)