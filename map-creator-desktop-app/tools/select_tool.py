from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtGui import QTransform
from PySide6.QtCore import Qt
from tools.tool import Tool
from map_presenter import MapPresenter
from commands.move_command import MoveElementsCommand
from commands.delete_command import DeleteElementsCommand

class SelectTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Select Tool"):
        super().__init__(presenter, scene, name)

        self._is_dragging = False
        self._start_pos = None
        self._reference_movable = None

        self._selected_models = set()
        self._movables_start_pos = dict()

    def deactivate(self):
        if self._is_dragging:
            for model, pos in self._movables_start_pos.items():
                model.position = pos
        
        self.clear_selection()
        self._reset_dragging()

    def mouse_click(self, pos, modifier=None):
        ctrl_active = modifier == Qt.ControlModifier
        item = self.scene.itemAt(pos, QTransform())

        if item is None:
            if not ctrl_active:
                self.clear_selection()
            return
        else:
            model = self.presenter.get_model_for_item(item)
            if model is None:
                return
            
            if not ctrl_active and model not in self._selected_models:
                self.clear_selection()
            
            self._selected_models.add(model)
            item.setSelected(True)

            movables = model.movables
            for movable in movables:
                self._movables_start_pos[movable] = movable.position
            
            self._is_dragging = True
            self._start_pos = pos
            self._reference_movable = movables[0]

    def mouse_move(self, pos):
        if self._is_dragging:
            delta = pos - self._start_pos
            delta = self.presenter.snap_to_grid(delta)

            if delta.manhattanLength() < 0.1:
                return

            for model in self._movables_start_pos.keys():
                model.position = self._movables_start_pos[model]
                model.moveBy(delta)
                
    def mouse_release(self, pos):
        if self._is_dragging and self._movables_start_pos:
            delta = pos - self._start_pos
            delta = self.presenter.snap_to_grid(delta)

            if delta.manhattanLength() > 0.1:
                for model, pos in self._movables_start_pos.items():
                    model.position = pos
                    pos = pos + delta

                cmd = MoveElementsCommand([x for x in self._movables_start_pos.keys()], delta)
                self.presenter.execute(cmd)
                
        else:
            self.clear_selection()
        
        self._reset_dragging()

    def key_press(self, key):
        if key == Qt.Key_Delete:
            elements_to_delete = [model for model in self._selected_models]
            cmd = DeleteElementsCommand(self.presenter, elements_to_delete)
            self.presenter.execute(cmd)

            self._reset_dragging()

    def _reset_dragging(self):
        self._is_dragging = False
        self._start_pos = None
        self._start_pos = None
        self._last_pos = None
        self._reference_movable = None

    def clear_selection(self):
        for model in self._selected_models:
            item = self.presenter.get_graphics_item_for_model(model)
            if item is not None:
                item.setSelected(False)

        self._selected_models.clear()
        self._movables_start_pos.clear()