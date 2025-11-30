from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtCore import QObject, Signal

from .node import Node
from .wall import Wall
from .zone import Zone
from .point_of_interest import PointOfInterest
from .map_object import MapObject

import weakref
import uuid

if TYPE_CHECKING:
    from .building import Building

class Floor(QObject):
    item_added = Signal(MapObject)
    item_removed = Signal(MapObject)
    name_changed = Signal(str)

    def __init__(self, name: str = "Unnamed Floor", id: uuid.UUID = None):
        super().__init__()
        self._name = name
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []
        self.zones: list[Zone] = []
        self.points_of_interest: list[PointOfInterest] = []

        self._uuid = id if id else uuid.uuid4()

        self._type_to_list = {
            Node: self.nodes,
            Wall: self.walls,
            Zone: self.zones,
            PointOfInterest: self.points_of_interest,
        }

    @property
    def building(self) -> Building:
        return self._building() if self._building else None
    
    @building.setter
    def building(self, building: Building):
        self._building = weakref.ref(building) if building else None

    @property
    def elements(self) -> list[MapObject]:
        all_elements = []
        for lst in self._type_to_list.values():
            all_elements.extend(lst)
        return all_elements

    @property
    def name(self) -> str:
        return self._name
    
    @name.setter
    def name(self, new_name: str):
        self._name = new_name
        self.name_changed.emit(new_name)

    def add(self, element: MapObject):
        if element is None:
            return

        el_type = type(element)
        el_list = self._get_list_for_type(el_type)
        
        if el_list is not None and element not in el_list:
            el_list.append(element)
            element.floor = self

            for dependency in element.dependencies:
                self.add(dependency)
                dependency.floor = self

            if hasattr(element, 'owner'):
                if element.owner:    
                    self.add(element.owner)
                    element.owner.floor = self

            self.item_added.emit(element)

    def remove(self, element: MapObject):
        if element is None:
            return

        el_type = type(element)
        el_list = self._get_list_for_type(el_type)
        
        if el_list is not None and element in el_list:
            el_list.remove(element)

            for dependency in element.dependencies:
                self.remove(dependency)

            if hasattr(element, 'owner'):
                self.remove(element.owner)

            self.item_removed.emit(element)

    def _get_list_for_type(self, el_type: type) -> list[MapObject] | None:
        return self._type_to_list.get(el_type, None)
    
    def to_dict(self) -> dict:
        return {
            "id": str(self._uuid),
            "name": self._name,
            "nodes": [node.to_dict() for node in self.nodes],
            "walls": [wall.to_dict() for wall in self.walls],
            "zones": [zone.to_dict() for zone in self.zones],
            "points_of_interest": [poi.to_dict() for poi in self.points_of_interest],
        }