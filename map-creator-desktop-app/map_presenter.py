from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject, Signal
from PySide6.QtGui import QUndoStack

from model.map_model import MapModel
from node_item import NodeGraphicsItem
from tools.tool import Tool
from view.wall_graphics_item import WallGraphicsItem
from model.wall import Wall
from model.node import Node

class MapPresenter(QObject):
    current_tool_changed = Signal(Tool)

    def __init__(self, model: MapModel, scene: QGraphicsScene):
        super().__init__()
        self.model = model
        self.scene = scene

        self._undo_stack = QUndoStack()

        self._current_tool = None
        self.model.node_added.connect(self._on_node_added)
        self.model.node_removed.connect(self._on_node_removed)

        self.model.wall_added.connect(self._on_wall_added)
        self.model.wall_removed.connect(self._on_wall_removed)
        self._item_map = {}


    # ------------------------------------
    # --------- Tool management ----------
    # ------------------------------------
    @property
    def current_tool(self):
        return self._current_tool

    @current_tool.setter
    def current_tool(self, tool):
        if type(tool) == type(self._current_tool):
            return
        
        self._current_tool = tool
        self.current_tool_changed.emit(tool)

    # ------------------------------------
    # ---- QUndoStack related methods ----
    # ------------------------------------
    def execute_command(self, command: QUndoStack):
        self._undo_stack.push(command)

    def undo(self):
        self._undo_stack.undo()

    def redo(self):
        self._undo_stack.redo()

    # ------------------------------------
    # ------ Model change handlers -------
    # ------------------------------------
    def _on_node_added(self, node: Node):
        new_node = NodeGraphicsItem(node)
        self.scene.addItem(new_node)
        self._item_map[node] = new_node

    def _on_node_removed(self, node: Node):
        item = self._item_map.get(node)
        if item:
            self.scene.removeItem(item)
            del self._item_map[node]

    def _on_wall_added(self, wall: Wall):
        new_wall_item = WallGraphicsItem(wall)
        self._item_map[wall] = new_wall_item
        self.scene.addItem(new_wall_item)

    def _on_wall_removed(self, wall: Wall):
        item = self._item_map.get(wall)
        if item:
            self.scene.removeItem(item)
            del self._item_map[wall]


    # ------------------------------------
    # ------- View event handling --------
    # ------------------------------------
    def on_canvas_click(self, pos):
        if self._current_tool:
            self._current_tool.mouse_click(pos)
        else:
            print("No tool selected.")

    def on_canvas_move(self, pos):
        if self._current_tool:
            self._current_tool.mouse_move(pos)