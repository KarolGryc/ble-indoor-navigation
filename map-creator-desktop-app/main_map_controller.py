from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import QObject, QPointF, Signal
from PySide6.QtGui import QUndoStack

from tools import Tool
from model import Floor, MapObject, Building

class MainMapController(QObject):
    pointer_canvas_moved = Signal(QPointF)

    def __init__(self, 
                 model: Building, 
                 scene: QGraphicsScene, 
                 grid_size: int = 50,
                 type_to_graphics_item: dict[type, type] = None):
        super().__init__()
        self.model = model
        self.scene = scene
        self._grid_size = grid_size
        self._current_tool = None
        self._show_grid = True
        self._model_to_view_map = {}
        self._view_to_model_map = {}
        self._undo_stack = QUndoStack()

        if not type_to_graphics_item:
            raise ValueError("type_to_graphics_item mapping must be provided.")

        self._model_class_to_view_class = type_to_graphics_item

        self._current_floor = self.model.get_floor(0)
        self._current_floor.item_added.connect(self._on_item_added)
        self._current_floor.item_removed.connect(self._on_item_removed)
        
        if self._current_floor is None:
            raise ValueError("Building must have at least one floor.")

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
    def current_tool(self, tool_type: type[Tool]):
        if tool_type == type(self._current_tool):
            return
        
        if self._current_tool is not None:
            self._current_tool.deactivate()

        self._current_tool = tool_type(self, self.scene)

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
    @property
    def building(self) -> Building:
        return self.model
    
    @building.setter
    def building(self, building: Building):
        self._undo_stack.clear()
        self._current_tool = type(self._current_tool)(self, self.scene) if self._current_tool else None
        self.model = building
        self._current_floor = self.model.get_floor(0)
        self._redraw_scene()

    @property
    def current_floor(self) -> Floor:
        return self._current_floor
    
    @current_floor.setter
    def current_floor(self, floor: Floor):
        if self._current_floor:
            self._current_floor.item_added.disconnect(self._on_item_added)
            self._current_floor.item_removed.disconnect(self._on_item_removed)

        self._current_floor = floor
        self._current_floor.item_added.connect(self._on_item_added)
        self._current_floor.item_removed.connect(self._on_item_removed)
        
        if self._current_tool:
            self._current_tool.deactivate()

        self._redraw_scene()

    def get_model_for_item(self, item) -> MapObject:
        return self._view_to_model_map.get(item, None)
    
    def get_item_for_model(self, model):
        return self._model_to_view_map.get(model, None)
    
    def _redraw_scene(self):
        self.scene.clear()
        self._model_to_view_map.clear()
        self._view_to_model_map.clear()

        floor = self._current_floor
        for element in floor.elements:
            self._on_item_added(element)

    def _on_item_added(self, item):
        view_class = self._model_class_to_view_class.get(type(item), None)
        if view_class is None:
            return

        new_item = view_class(item)
        self._model_to_view_map[item] = new_item
        self._view_to_model_map[new_item] = item
        self.scene.addItem(new_item)

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
        self.pointer_canvas_moved.emit(pos)

        if self._current_tool is not None:
            self._current_tool.mouse_move(pos)

    def on_canvas_release(self, pos):
        if self._current_tool is not None:
            self._current_tool.mouse_release(pos)

    def on_keyboard_press(self, key):
        if self._current_tool is not None:
            self._current_tool.key_press(key)