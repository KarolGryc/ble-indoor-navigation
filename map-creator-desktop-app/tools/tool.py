from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtWidgets import QGraphicsScene

from abc import ABC, abstractmethod

if TYPE_CHECKING:
    from main_map_controller import MainMapController

class Tool(ABC):
    def __init__(self, 
                 controller: MainMapController, 
                 scene: QGraphicsScene, 
                 name="Generic Tool"):
        self._controller = controller
        self._scene = scene
        self.name = name

    def mouse_click(self, pos, modifier=None):
        pass

    def mouse_move(self, pos):
        pass

    def mouse_release(self, pos):
        pass

    def deactivate(self):
        pass

    def key_press(self, key):
        pass