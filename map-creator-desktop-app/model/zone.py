import uuid
from PySide6.QtCore import QPointF

from enum import Enum

from .node import Node
from .map_object import MapObject

class ZoneType(Enum):
    GENERIC = 1
    STAIRS = 2
    ELEVATOR = 3

class Zone(MapObject):
    def __init__(self, 
                 corner_nodes: list[Node], 
                 name="Zone", 
                 type=ZoneType.GENERIC,
                 id:uuid.UUID=None):
        super().__init__(id)
        self.corner_nodes = corner_nodes
        self._name = name
        self._type = type if type is not None else ZoneType.GENERIC

        for node in self.corner_nodes:
            node.owner = self
            node.updated.connect(self.updated)

    @property
    def dependencies(self) -> list[Node]:
        return self.corner_nodes

    @property
    def name(self) -> str:
        return self._name
    
    @name.setter
    def name(self, value: str) -> None:
        self._name = value
        self.updated.emit()

    @property
    def type(self) -> ZoneType:
        return self._type
    
    @type.setter
    def type(self, value: ZoneType) -> None:
        self._type = value
        self.updated.emit()

    @property
    def movables(self) -> list[Node]:
        return self.corner_nodes
    
    @property
    def position(self) -> QPointF:
        x = sum(node.x for node in self.corner_nodes) / len(self.corner_nodes)
        y = sum(node.y for node in self.corner_nodes) / len(self.corner_nodes)
        return QPointF(x, y)
    
    @position.setter
    def position(self, pos: QPointF) -> None:
        dx = pos.x() - self.position.x()
        dy = pos.y() - self.position.y()
        delta = QPointF(dx, dy)

        self.moveBy(delta)
        self.updated.emit()

    def moveBy(self, delta: QPointF) -> None:
        for node in self.corner_nodes:
            node.moveBy(delta)

        self.updated.emit()

    def to_dict(self) -> dict:
        return {
            "id": str(self.uuid),
            "name": self._name,
            "type": self._type.name,
            "corner_node_ids": [str(node.uuid) for node in self.corner_nodes],
        }