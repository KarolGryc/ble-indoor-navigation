from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtGui import QTransform
from tools.tool import Tool
from map_presenter import MapPresenter
from commands.element_move_command import MoveElementsCommand

class SelectTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Select Tool"):
        super().__init__(presenter, scene, name)

        self._is_dragging = False
        self._dragged_item = None
        self._start_pos = None
        self._last_pos = None

    def deactivate(self):
        if self._dragged_item is not None:
            self._dragged_item.setOpacity(1)

        if self._is_dragging and self._dragged_item is not None:
            self._dragged_item.setPos(self._start_pos)
            pass

        self._is_dragging = False
        self._dragged_item = None
        self._start_pos = None
        self._last_pos = None

    def mouse_click(self, pos):
        item = self.scene.itemAt(pos, QTransform())
        if item is None:
            # Clear selection
            return

        self._dragged_item = item
        self._is_dragging = True
        self._start_pos = item.pos()
        self._last_pos = item.pos()
        self._dragged_item.setOpacity(0.5)

    def mouse_move(self, pos):
        if self._is_dragging:
            delta = pos - self._last_pos
            delta = self.presenter.snap_to_grid(delta)

            if delta.manhattanLength() < 0.1:
                return

            self._dragged_item.moveBy(delta.x(), delta.y())
            self._last_pos = self.presenter.snap_to_grid(self._dragged_item.pos())

    def mouse_release(self, pos):
        if self._is_dragging and self._dragged_item is not None:
            self._dragged_item.setOpacity(1)
            self._dragged_item.setPos(self._start_pos)

            if self._last_pos != self._start_pos:
                model_element = self.presenter.get_model_for_item(self._dragged_item)
                delta = self._last_pos - self._start_pos
                cmd = MoveElementsCommand(self.presenter.model, [model_element], delta)
                self.presenter.execute_command(cmd)

        self._is_dragging = False
        self._dragged_item = None
        self._start_pos = None
        self._last_pos = None
