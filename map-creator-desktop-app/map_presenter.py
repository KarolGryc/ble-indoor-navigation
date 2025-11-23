from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject
from map_model import MapModel
from wall import Wall
from node import Node

class MapPresenter(QObject):
    def __init__(self, model: MapModel, scene: QGraphicsScene):
        super().__init__()
        self.model = model
        self.scene = scene

        self.first_click = None

    def on_canvas_click(self, pos):
        if self.first_click is None:
            self.first_click = pos
        else:
            start_node = Node(self.first_click.x(), self.first_click.y())
            end_node = Node(pos.x(), pos.y())
            self.model.nodes.extend([start_node, end_node])
            wall = Wall(start_node, end_node)
            self.model.walls.append(wall)
            self.scene.addLine(wall.start_node.x, wall.start_node.y, wall.end_node.x, wall.end_node.y)
            self.first_click = None