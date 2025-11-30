from PySide6.QtGui import QUndoCommand
from PySide6.QtCore import QPointF

from model import MapObject

class MoveElementsCommand(QUndoCommand):
    def __init__(self, elements: list[MapObject], delta_pos: QPointF):
        super().__init__("Move Element")
        self._elements = elements
        self._delta_pos = delta_pos

    def redo(self):
        for element in self._elements:
            element.moveBy(self._delta_pos)

    def undo(self):
        for element in self._elements:
            element.moveBy(-self._delta_pos)