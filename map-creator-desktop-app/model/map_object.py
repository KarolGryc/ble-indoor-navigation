from PySide6.QtCore import QObject, Signal

import weakref

class MapObject(QObject):
    updated = Signal()

    def __init__(self):
        super().__init__()

    @property
    def floor(self) -> "Floor":
        return self._floor() if self._floor else None
    
    @floor.setter
    def floor(self, new_floor: "Floor"):
        self._floor = weakref.ref(new_floor) if new_floor else None

    @property
    def movables(self):
        pass

    @property
    def dependencies(self) -> list:
        return []