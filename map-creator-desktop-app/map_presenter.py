from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject, Signal, QPointF
from PySide6.QtGui import QUndoStack

from model.floor import Floor
from tools.tool import Tool
from view.node_item import NodeGraphicsItem
from view.wall_item import WallGraphicsItem
from view.zone_item import ZoneGraphicsItem
from view.point_of_interest_item import PointOfInterestGraphicsItem
from model.wall import Wall
from model.node import Node
from model.zone import Zone
from model.point_of_interest import PointOfInterest
from model.map_object import MapObject
from model.building import Building

class MapPresenter(QObject):
    scene_changed = Signal(QGraphicsScene)

    def __init__(self, 
                 model: Building, 
                 scene: QGraphicsScene, 
                 grid_size: int = 50):
        super().__init__()
        self.model = model
        self.scene = scene
        self._grid_size = grid_size

        self._undo_stack = QUndoStack()

        self._current_tool = None

        self_current_floor = self.model.get_floor(0)
        if self_current_floor is None:
            raise ValueError("Building must have at least one floor.")
        

        # self.model.item_added.connect(self._on_item_added)
        # self.model.item_removed.connect(self._on_item_removed)

        self._model_to_view_map = {}
        self._view_to_model_map = {}

        self._show_grid = True

        self._model_class_to_view_class = {
            Node: NodeGraphicsItem,
            Wall: WallGraphicsItem,
            Zone: ZoneGraphicsItem,
            PointOfInterest: PointOfInterestGraphicsItem,
        }

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
    def get_model_for_item(self, item) -> MapObject:
        return self._view_to_model_map.get(item, None)
    
    def get_item_for_model(self, model):
        return self._model_to_view_map.get(model, None)

    def _on_item_added(self, item):
        view_class = self._model_class_to_view_class.get(type(item), None)
        if view_class is None:
            return
        
        new_item = view_class(item)
        self._model_to_view_map[item] = new_item
        self._view_to_model_map[new_item] = item
        self.scene.addItem(new_item)
        item.updated.connect(lambda : new_item.update_item())

    def _on_item_removed(self, element):
        item = self._model_to_view_map.pop(element, None)
        if item:
            self.scene.removeItem(item)
            del self._view_to_model_map[item]

    # ------------------------------------
    # ------- View event handling --------
    # ------------------------------------
    def on_canvas_click(self, pos, modifier=None):
        if self._current_tool is not None:
            self._current_tool.mouse_click(pos, modifier)

    def on_canvas_move(self, pos):
        if self._current_tool is not None:
            self._current_tool.mouse_move(pos)

    def on_canvas_release(self, pos):
        if self._current_tool is not None:
            self._current_tool.mouse_release(pos)

    def on_keyboard_press(self, key):
        if self._current_tool is not None:
            self._current_tool.key_press(key)