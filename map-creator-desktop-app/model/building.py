from PySide6.QtCore import QObject, Signal

from .floor import Floor

class Building(QObject):
    floor_added = Signal(Floor)
    floor_removed = Signal(Floor)
    floor_name_changed = Signal(str)

    def __init__(self):
        super().__init__()
        self._floors: list[Floor] = []
        self._zone_connections = {}

    def add_connection(self, zone1, zone2):
        self._zone_connections.setdefault(zone1, set()).add(zone2)
        self._zone_connections.setdefault(zone2, set()).add(zone1)

    def remove_connection(self, zone1, zone2):
        self._zone_connections.get(zone1, set()).discard(zone2)
        self._zone_connections.get(zone2, set()).discard(zone1)

    def get_zones_connected_to(self, zone):
        return self._zone_connections.get(zone, set())
        
    def add_floor(self, floor: Floor = Floor()):
        if floor not in self._floors:
            floor.building = self
            self._floors.append(floor)
            self.floor_added.emit(floor)
            floor.name_changed.connect(self.floor_name_changed)

    def remove_floor(self, floor: Floor):
        if floor in self._floors:
            self._floors.remove(floor)
            self.floor_removed.emit(floor)
            floor.name_changed.disconnect(self.floor_name_changed)

    def remove_floor_at(self, index: int):
        if 0 <= index < len(self._floors):
            floor = self._floors.pop(index)
            self.floor_removed.emit(floor)

    def get_floor(self, index: int) -> Floor | None:
        if 0 <= index < len(self._floors):
            return self._floors[index]
        return None
    
    @property
    def floors(self) -> list[Floor]:
        return self._floors
    
    @floors.setter
    def floors(self, new_floors: list[Floor]):
        self._floors = new_floors