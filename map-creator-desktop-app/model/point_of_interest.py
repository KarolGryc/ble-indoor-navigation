import uuid
from PySide6.QtCore import QPointF

from enum import Enum

from .map_object import MapObject

class PointOfInterestType(Enum):
    GENERIC = 1
    RESTAURANT = 2
    SHOP = 3
    TOILET = 4
    EXIT = 5

class PointOfInterest(MapObject):
    def __init__(self, 
                 position: QPointF, 
                 name="Point of Interest", 
                 type=PointOfInterestType.GENERIC,
                 id:uuid.UUID=None):
        super().__init__(id)
        self._position: QPointF = position
        self._name: str = name
        self._type: PointOfInterestType = type

    @property
    def name(self) -> str:
        return self._name
    
    @name.setter
    def name(self, value: str):
        self._name = value
        self.updated.emit()

    @property
    def movables(self) -> list:
        return [self]
    
    def moveBy(self, delta: QPointF):
        self._position = self._position + delta
        self.updated.emit()

    @property
    def position(self) -> QPointF:
        return self._position
    
    @position.setter
    def position(self, pos: QPointF):
        self._position = pos
        self.updated.emit()

    @property
    def type(self) -> PointOfInterestType:
        return self._type
    
    @type.setter
    def type(self, value: PointOfInterestType):
        self._type = value
        self.updated.emit()

    def to_dict(self) -> dict:
        return {
            "id": str(self.uuid),
            "x": self._position.x(), 
            "y": self._position.y(),
            "name": self._name,
            "type": self._type.name,
        }