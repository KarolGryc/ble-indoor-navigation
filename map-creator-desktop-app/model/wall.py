from PySide6.QtCore import QPointF
from model.node import Node
from model.map_object import MapObject

class Wall(MapObject):
    def __init__(self, start_node: Node, end_node: Node):
        super().__init__()
        self.start_node = start_node
        self.end_node = end_node
        self.start_node.owner = self
        self.end_node.owner = self

        self.start_node.updated.connect(self.updated)
        self.end_node.updated.connect(self.updated)

    def length(self) -> float:
        return self.start_node.distance_to(self.end_node)
    
    @property
    def movables(self) -> list[Node]:
        return [self.start_node, self.end_node]

    @property
    def position(self) -> QPointF:
        return self.middle_point()
    
    @position.setter
    def position(self, pos: QPointF):
        dx = pos.x() - self.middle_point().x()
        dy = pos.y() - self.middle_point().y()

        delta = QPointF(dx, dy)
        self.moveBy(delta)
    
    def moveBy(self, delta: QPointF) -> None:
        self.start_node.position = self.start_node.position + delta
        self.end_node.position = self.end_node.position + delta

        self.updated.emit()
    
    def middle_point(self) -> QPointF:
        x = (self.start_node.x + self.end_node.x) / 2
        y = (self.start_node.y + self.end_node.y) / 2
        return QPointF(x, y)