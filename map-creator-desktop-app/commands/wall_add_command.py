from PySide6.QtGui import QUndoCommand
from model import Node, Wall

class WallAddCommand(QUndoCommand):
    def __init__(self, model, start_pos, end_pos):
        super().__init__("Add Wall")
        self._model = model
        self.start_node = Node(start_pos.x(), start_pos.y())
        self.end_node = Node(end_pos.x(), end_pos.y())
        self.wall = Wall(self.start_node, self.end_node)

    def redo(self):
        self._model.add(self.wall)

    def undo(self):
        self._model.remove(self.wall)