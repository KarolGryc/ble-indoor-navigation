from PySide6.QtGui import QUndoCommand

from model.map_model import MapModel
from model.node import Node

class NodeAddCommand(QUndoCommand):
    def __init__(self, model: MapModel, node: Node):
        super().__init__("Add Node")
        self.model = model
        self.node = node

    def redo(self):
        self.model.add_node(self.node)

    def undo(self):
        self.model.remove_node(self.node)