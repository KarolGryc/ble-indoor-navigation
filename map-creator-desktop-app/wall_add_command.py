from wall import Wall
from node import Node
from PySide6.QtGui import QUndoCommand

class WallAddCommand(QUndoCommand):
    def __init__(self, model, start_pos, end_pos):
        super().__init__("Add Wall")
        self.model = model
        self.start_node = Node(start_pos.x(), start_pos.y())
        self.end_node = Node(end_pos.x(), end_pos.y())
        self.wall = Wall(self.start_node, self.end_node)

    def redo(self):
        self.model.add_wall(self.wall)
        self.model.add_node(self.start_node)
        self.model.add_node(self.end_node)

    def undo(self):
        self.model.remove_wall(self.wall)
        self.model.remove_node(self.start_node)
        self.model.remove_node(self.end_node)