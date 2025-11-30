from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtCore import QObject, Signal

import uuid
import weakref

if TYPE_CHECKING:
    from .floor import Floor
    

class MapObject(QObject):
    updated = Signal()

    def __init__(self, id: uuid.UUID = None):
        super().__init__()
        self.uuid = id if id else uuid.uuid4()

    @property
    def floor(self) -> Floor:
        return self._floor() if self._floor else None
    
    @floor.setter
    def floor(self, new_floor: Floor):
        self._floor = weakref.ref(new_floor) if new_floor else None

    @property
    def movables(self):
        pass

    @property
    def dependencies(self) -> list:
        return []