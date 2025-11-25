from PySide6.QtGui import QUndoCommand
from model.map_model import MapModel

class MoveElementsCommand(QUndoCommand):
    def __init__(self, elements, delta_pos):
        super().__init__("Move Element")
        self._elements = elements
        self._delta_pos = delta_pos

    def redo(self):
        for element in self._elements:
            element.moveBy(self._delta_pos)

    def undo(self):
        for element in self._elements:
            element.moveBy(-self._delta_pos)