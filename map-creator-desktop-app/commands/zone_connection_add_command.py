from PySide6.QtGui import QUndoCommand
from model.building import Building
from model.zone import Zone

class ZoneConnectionAddCommand(QUndoCommand):
    def __init__(self, building: Building, zone_a: Zone, zone_b: Zone):
        super().__init__("Add Zone")
        self._building = building
        self._zone_a = zone_a
        self._zone_b = zone_b
        
    def redo(self):
        self._building.add_connection(self._zone_a, self._zone_b)

    def undo(self):
        self._building.remove_connection(self._zone_a, self._zone_b)