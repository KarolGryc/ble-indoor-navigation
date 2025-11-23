from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject
from PySide6.QtGui import QUndoStack

from map_model import MapModel
from node_item import NodeGraphicsItem
from wall_graphics_item import WallGraphicsItem
from wall import Wall
from node import Node

class MapPresenter(QObject):
    def __init__(self, model: MapModel, scene: QGraphicsScene):
        super().__init__()
        self.model = model
        self.scene = scene

        self.first_click = None
        self.undo_stack = QUndoStack()

        self._current_tool = None
        self.model.node_added.connect(self._on_node_added)
        self.model.node_removed.connect(self._on_node_removed)

        self.model.wall_added.connect(self._on_wall_added)
        self.model.wall_removed.connect(self._on_wall_removed)
        self._item_map = {}

    def _on_node_added(self, node: Node):
        new_node = NodeGraphicsItem(node)
        self.scene.addItem(new_node)
        self._item_map[node] = new_node
        pass 

    def _on_node_removed(self, node: Node):
        item = self._item_map.get(node)
        if item:
            self.scene.removeItem(item)
            del self._item_map[node]
            print(f"Node removed at ({node.x}, {node.y})")
        pass

    def _on_wall_added(self, wall: Wall):
        new_wall_item = WallGraphicsItem(wall)
        self._item_map[wall] = new_wall_item
        self.scene.addItem(new_wall_item)
        print(f"Wall added: {wall}")
        pass

    def _on_wall_removed(self, wall: Wall):
        item = self._item_map.get(wall)
        if item:
            self.scene.removeItem(item)
            del self._item_map[wall]

        print(f"Wall removed: {wall}")
        pass

    def on_canvas_click(self, pos):
        if self._current_tool:
            self._current_tool.mouse_click(pos)
        else:
            print("No tool selected.")

    def on_canvas_move(self, pos):
        if self._current_tool:
            self._current_tool.mouse_move(pos)

    def set_current_tool(self, tool):
        self._current_tool = tool