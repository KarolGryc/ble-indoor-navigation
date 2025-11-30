from PySide6.QtWidgets import QGraphicsScene

from abc import ABC, abstractmethod

class Tool(ABC):
    def __init__(self, presenter, scene: QGraphicsScene, name="Generic Tool"):
        self._presenter = presenter
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