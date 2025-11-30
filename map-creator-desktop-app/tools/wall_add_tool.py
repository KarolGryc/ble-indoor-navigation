from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtWidgets import QGraphicsScene

from .tool import Tool
from view import WallPreview
from commands import WallAddCommand

if TYPE_CHECKING:
    from main_map_controller import MainMapController

class WallAddTool(Tool):
    def __init__(self, presenter: MainMapController, scene: QGraphicsScene, name="Add Wall"):
        super().__init__(presenter, scene, name)

        self._start_point = None
        self._preview = WallPreview(scene)

    def deactivate(self):
        self._start_point = None
        self._preview.clear()

    def mouse_click(self, pos, modifier=None):
        pos = self._controller.snap_to_grid(pos)
        self._preview.update_preview(self._start_point, pos)

        if self._start_point is None:
            self._start_point = pos
        else:
            command = WallAddCommand(self._controller.current_floor, self._start_point, pos)
            self._controller.execute(command)
            self.deactivate()

    def mouse_move(self, pos):
        pos = self._controller.snap_to_grid(pos)
        self._preview.update_preview(self._start_point, pos)