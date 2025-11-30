from PySide6.QtGui import QUndoCommand

from model import Zone, Node

class ZoneAddCommand(QUndoCommand):
    def __init__(self, model, corner_points, name = "", zone_type=None):
        super().__init__("Add Zone")
        self._model = model
        self._corner_points = corner_points
        self._zone = Zone([Node(pt.x(), pt.y()) for pt in corner_points], name, zone_type)
        
    def redo(self):
        self._model.add(self._zone)

    def undo(self):
        self._model.remove(self._zone)