from PySide6.QtCore import QPointF, Signal
from math import sqrt
from model.map_object import MapObject

class Node(MapObject):
    def __init__(self, x: float, y: float):
        super().__init__()
        self.pos = QPointF(x, y)
        self.wall = None

    @property
    def movables(self):
        return [self]

    @property
    def position(self) -> QPointF:
        return self.pos
    
    @position.setter
    def position(self, pos: QPointF):
        if self.pos != pos:
            self.pos = pos
            self.updated.emit()

    @property
    def x(self) -> float:
        return self.pos.x()
    
    @x.setter
    def x(self, value: float):
        if self.pos.x() != value:
            self.pos.setX(value)
            self.updated.emit()

    @property
    def y(self) -> float:
        return self.pos.y()
    
    @y.setter
    def y(self, value: float):
        if self.pos.y() != value:
            self.pos.setY(value)
            self.updated.emit()

    def moveBy(self, delta: QPointF):
        self.position = self.position + delta
        self.updated.emit()
        
    def distance_to(self, other: 'Node') -> float:
        return sqrt((self.x - other.x) ** 2 + (self.y - other.y) ** 2)