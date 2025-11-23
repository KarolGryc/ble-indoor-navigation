from PySide6.QtCore import QObject
from node import Node
from wall import Wall

class MapModel(QObject):
    def __init__(self):
        super().__init__()
        self.nodes: list[Node] = []
        self.walls: list[Wall] = []