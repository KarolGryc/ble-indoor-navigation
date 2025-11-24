from PySide6.QtCore import QObject, Signal, QPointF
from model.node import Node


class Wall(QObject):
    geometry_changed = Signal()

    def __init__(self, start_node: Node, end_node: Node):
        super().__init__()
        self.start_node = start_node
        self.end_node = end_node

        self.start_node.position_changed.connect(self._on_node_changed)
        self.end_node.position_changed.connect(self._on_node_changed)
        
    def _on_node_changed(self):
        self.geometry_changed.emit()

    def length(self) -> float:
        return self.start_node.distance_to(self.end_node)