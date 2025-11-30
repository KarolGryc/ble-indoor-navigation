from PySide6.QtGui import QUndoCommand

from model import PointOfInterest, PointOfInterestType

class PointOfInterestAddCommand(QUndoCommand):
    def __init__(self, model, position, name="Place", type=PointOfInterestType.GENERIC):
        super().__init__("Add Point of Interest")
        self._model = model
        self._point_of_interest = PointOfInterest(position, name, type)

    def redo(self):
        self._model.add(self._point_of_interest)

    def undo(self):
        self._model.remove(self._point_of_interest)