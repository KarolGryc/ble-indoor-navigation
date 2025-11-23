from PySide6.QtCore import QObject, Signal
from node import Node

class Wall(QObject):
    def __init__(self, start_node: Node, end_node: Node):
        super().__init__()
        self.start_node = start_node
        self.end_node = end_node

    def on_node_changed(self):
        self.wall_changed.emit()

    def length(self) -> float:
        return self.start_node.distance_to(self.end_node)