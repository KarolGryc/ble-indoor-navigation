from PySide6.QtGui import QUndoCommand

from model import Zone

class ZoneAttributesChangedCommand(QUndoCommand):
    def __init__(self, zone: Zone, new_name: str, new_type: str):
        super().__init__("Change Zone Attributes")
        self._zone = zone
        self._old_name = zone.name
        self._old_type = zone.zone_type
        self._new_name = new_name
        self._new_type = new_type
        
    def redo(self):
        self._zone.name = self._new_name
        self._zone.zone_type = self._new_type

    def undo(self):
        self._zone.name = self._old_name
        self._zone.zone_type = self._old_type

