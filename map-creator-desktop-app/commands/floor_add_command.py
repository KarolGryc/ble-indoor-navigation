from PySide6.QtGui import QUndoCommand

from model import Building, Floor

class FloorAddCommand(QUndoCommand):
    def __init__(self, building: Building, floor: Floor):
        super().__init__("Add Floor")
        self._building = building
        self._floor = floor if floor is not None else Floor()

    def redo(self):
        self._building.add_floor(self._floor)

    def undo(self):
        self._building.remove_floor(self._floor)