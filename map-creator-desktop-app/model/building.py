from model.floor import Floor
from PySide6.QtCore import QObject, Signal

class Building(QObject):
    floor_added = Signal(Floor)
    floor_removed = Signal(Floor)

    def __init__(self):
        super().__init__()
        self._floors: list[Floor] = []

    def add_floor(self, floor: Floor = Floor()):
        if floor not in self._floors:
            self._floors.append(floor)
            self.floor_added.emit(floor)

    def remove_floor(self, floor: Floor):
        if floor in self._floors:
            self._floors.remove(floor)
            self.floor_removed.emit(floor)
        
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