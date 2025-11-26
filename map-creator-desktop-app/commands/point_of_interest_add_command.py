from model.point_of_interest import PointOfInterest, PointOfInterestType
from PySide6.QtGui import QUndoCommand

class PointOfInterestAddCommand(QUndoCommand):
    TYPE_COUNTER = 0

    def __init__(self, model, position, name="POMOCY", type=PointOfInterestType.GENERIC):
        super().__init__("Add Point of Interest")
        self._model = model
        self._point_of_interest = PointOfInterest(position, name, type)

    def redo(self):
        self._model.add_point_of_interest(self._point_of_interest)

    def undo(self):
        self._model.remove_point_of_interest(self._point_of_interest)