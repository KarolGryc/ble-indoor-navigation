from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject, Signal, QPointF
from PySide6.QtGui import QUndoStack

from model.map_model import MapModel
from view.node_item import NodeGraphicsItem
from tools.tool import Tool
from view.wall_graphics_item import WallGraphicsItem
from model.wall import Wall
from model.node import Node

class MapPresenter(QObject):
    current_tool_changed = Signal(Tool)

    def __init__(self, model: MapModel, scene: QGraphicsScene, grid_size: int = 50):
        super().__init__()
        self.model = model
        self.scene = scene
        self._grid_size = grid_size

        self._undo_stack = QUndoStack()

        self._current_tool = None
        self.model.node_added.connect(self._on_node_added)
        self.model.node_removed.connect(self._on_node_removed)

        self.model.wall_added.connect(self._on_wall_added)
        self.model.wall_removed.connect(self._on_wall_removed)

        self._model_to_view_map = {}
        self._view_to_model_map = {}

        self._show_grid = True

    # ------------------------------------
    # ---------- Grid methods ------------
    # ------------------------------------
    @property
    def grid_size(self) -> int:
        return self._grid_size
    
    @grid_size.setter
    def grid_size(self, size: int):
        self._grid_size = size if size > 0 else 0

    def snap_to_grid(self, pos: QPointF) -> QPointF:
        x = round(pos.x() / self._grid_size) * self._grid_size
        y = round(pos.y() / self._grid_size) * self._grid_size
        return QPointF(x, y)
    
    @property
    def show_grid(self) -> bool:
        return self._show_grid
    
    @show_grid.setter
    def show_grid(self, show: bool):
        self._show_grid = show
        self.scene.update()

    # ------------------------------------
    # --------- Tool management ----------
    # ------------------------------------
    @property
    def current_tool(self) -> Tool:
        return self._current_tool

    @current_tool.setter
    def current_tool(self, tool: Tool):
        if type(tool) == type(self._current_tool):
            return
        
        if self._current_tool is not None:
            self._current_tool.deactivate()

        self._current_tool = tool
        self.current_tool_changed.emit(tool)

    def reset_current_tool(self):
        if self._current_tool is not None:
            self._current_tool.deactivate()

    # ------------------------------------
    # ---- QUndoStack related methods ----
    # ------------------------------------
    def execute(self, command: QUndoStack):
        self._undo_stack.push(command)

    def undo(self):
        self._undo_stack.undo()

    def redo(self):
        self._undo_stack.redo()

    # ------------------------------------
    # ------ Model change handlers -------
    # ------------------------------------
    def get_model_for_item(self, item):
        return self._view_to_model_map.get(item, None)

    def _on_node_added(self, node: Node):
        new_node = NodeGraphicsItem(node)
        self.scene.addItem(new_node)
        self._model_to_view_map[node] = new_node
        self._view_to_model_map[new_node] = node
        node.position_changed.connect(lambda pos: new_node.setPos(pos))

    def _on_node_removed(self, node: Node):
        item = self._model_to_view_map.pop(node, None)
        if item:
            self.scene.removeItem(item)
            del self._view_to_model_map[item]

    def _on_wall_added(self, wall: Wall):
        new_wall_item = WallGraphicsItem(wall)
        self.scene.addItem(new_wall_item)
        self._model_to_view_map[wall] = new_wall_item
        self._view_to_model_map[new_wall_item] = wall
        wall.geometry_changed.connect(lambda: new_wall_item.update_geometry())

    def _on_wall_removed(self, wall: Wall):
        item = self._model_to_view_map.pop(wall, None)
        if item:
            self.scene.removeItem(item)
            del self._view_to_model_map[item]

    # ------------------------------------
    # ------- View event handling --------
    # ------------------------------------
    def on_canvas_click(self, pos):
        if self._current_tool is not None:
            if hasattr(self._current_tool, 'mouse_click'):
                self._current_tool.mouse_click(pos)
        else:
            print("No tool selected.")

    def on_canvas_move(self, pos):
        if self._current_tool is not None:
            if hasattr(self._current_tool, 'mouse_move'):
                self._current_tool.mouse_move(pos)

    def on_canvas_release(self, pos):
        if self._current_tool is not None:
            if hasattr(self._current_tool, 'mouse_release'):
                self._current_tool.mouse_release(pos)