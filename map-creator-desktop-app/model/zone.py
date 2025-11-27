from PySide6.QtCore import Signal, QPointF
from model.node import Node
from model.map_object import MapObject

class Zone(MapObject):
    def __init__(self, corner_nodes: list[Node], name="Zone"):
        super().__init__()
        self.corner_nodes = corner_nodes
        self._name = name

        for node in self.corner_nodes:
            node.owner = self
            node.updated.connect(self._on_node_changed)

    def _on_node_changed(self):
        self.updated.emit()

    @property
    def name(self) -> str:
        return self._name
    
    @name.setter
    def name(self, value: str):
        self._name = value
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
            node.position = node.position + delta

        self.updated.emit()