from model.building import Building
from model.floor import Floor

from PySide6.QtGui import QUndoCommand

class FloorRemoveCommand(QUndoCommand):
    def __init__(self, building: Building, floor: Floor):
        super().__init__("Remove Floor")
        self._building = building
        self._floor = floor

    def redo(self):
        self._building.remove_floor(self._floor)

    def undo(self):
        self._building.add_floor(self._floor)