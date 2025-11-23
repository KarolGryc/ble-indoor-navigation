from tool import Tool

from map_presenter import MapPresenter
from node_add_command import NodeAddCommand
from node import Node

class NodeAddTool(Tool):
    def __init__(self, presenter: MapPresenter):
        self.presenter = presenter
    
    def mouse_click(self, pos):
        command = NodeAddCommand(self.presenter.model, Node(pos.x(), pos.y()))
        self.presenter.undo_stack.push(command)