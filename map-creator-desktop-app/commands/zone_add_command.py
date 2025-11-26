from model.zone import Zone
from model.node import Node
from PySide6.QtGui import QUndoCommand

class ZoneAddCommand(QUndoCommand):
    def __init__(self, model, corner_points):
        super().__init__("Add Zone")
        self._model = model
        self._corner_points = corner_points
        self._zone = Zone([Node(pt.x(), pt.y()) for pt in corner_points])

    def redo(self):
        self._model.add_zone(self._zone)
        for node in self._zone.corner_nodes:
            self._model.add_node(node)

    def undo(self):
        self._model.remove_zone(self._zone)
        for node in self._zone.corner_nodes:
            self._model.remove_node(node)