from tools.tool import Tool

from map_presenter import MapPresenter
from commands.node_add_command import NodeAddCommand
from PySide6.QtWidgets import QGraphicsScene
from model.node import Node

class NodeAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Node Add Tool"):
        super().__init__(presenter, scene, name)
    
    def mouse_click(self, pos):
        command = NodeAddCommand(self.presenter.model, Node(pos.x(), pos.y()))
        self.presenter.execute_command(command)

    def mouse_move(self, pos):
        pass