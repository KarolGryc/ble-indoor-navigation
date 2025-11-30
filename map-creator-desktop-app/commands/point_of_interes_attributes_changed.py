from PySide6.QtGui import QUndoCommand

from model import PointOfInterest

class PointOfInterestAttributesChangedCommand(QUndoCommand):
    def __init__(self, poi: PointOfInterest, new_name: str, new_description: str):
        super().__init__("Change Point of Interest Attributes")
        self._poi = poi
        self._old_name = poi.name
        self._old_description = poi.description
        self._new_name = new_name
        self._new_description = new_description
        
    def redo(self):
        self._poi.name = self._new_name
        self._poi.description = self._new_description

    def undo(self):
        self._poi.name = self._old_name
        self._poi.description = self._old_description