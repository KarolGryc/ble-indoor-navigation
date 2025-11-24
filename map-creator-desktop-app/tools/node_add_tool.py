from tools.tool import Tool

from map_presenter import MapPresenter
from commands.node_add_command import NodeAddCommand
from PySide6.QtWidgets import QGraphicsScene
from model.node import Node
from view.node_item import NodeGraphicsItem

class NodeAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Node Add Tool"):
        super().__init__(presenter, scene, name)
        self.node_add_preview = None

    def deactivate(self):
        if self.node_add_preview:
            self.scene.removeItem(self.node_add_preview)
            self.node_add_preview = None

    def mouse_click(self, pos):
        pos = self.presenter.snap_to_grid(pos)
        command = NodeAddCommand(self.presenter.model, Node(pos.x(), pos.y()))
        self.presenter.execute(command)

        if self.node_add_preview:
            self.scene.removeItem(self.node_add_preview)
            self.node_add_preview = None

    def mouse_move(self, pos):
        snapped_pos = self.presenter.snap_to_grid(pos)

        if self.node_add_preview is None:
            self.node_add_preview = NodeGraphicsItem(Node(snapped_pos.x(), snapped_pos.y()))
            self.node_add_preview.setOpacity(0.5)
            self.scene.addItem(self.node_add_preview)
        else:
            self.node_add_preview.setPos(snapped_pos)