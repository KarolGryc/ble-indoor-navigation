from abc import ABC, abstractmethod

from PySide6.QtWidgets import QGraphicsScene

class Tool(ABC):
    def __init__(self, presenter, scene: QGraphicsScene, name="Generic Tool"):
        self.presenter = presenter
        self.scene = scene
        self.name = name

    def mouse_click(self, pos):
        pass

    def mouse_move(self, pos):
        pass

    def mouse_release(self, pos):
        pass

    def deactivate(self):
        pass