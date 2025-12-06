from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtWidgets import QGraphicsScene

from utils.general import ask_poi_name_and_type

from .tool import Tool
from view  import PointOfInterestPreview
from commands import PointOfInterestAddCommand

if TYPE_CHECKING:
    from main_map_controller import MainMapController

class PointOfInterestAddTool(Tool):
    name = "Add Place"

    def __init__(self, presenter: MainMapController, scene: QGraphicsScene):
        super().__init__(presenter, scene)
        self._preview = PointOfInterestPreview(scene)

    def deactivate(self):
        self._preview.clear()
        return super().deactivate()

    def mouse_click(self, pos, modifier=None):
        snapped_pos = self._controller.snap_to_grid(pos)

        name, poi_type = ask_poi_name_and_type(window_name="Enter Point Details")

        if name is None or poi_type is None:
            return

        cmd = PointOfInterestAddCommand(self._controller.current_floor, snapped_pos, name, poi_type)
        self._controller.execute(cmd)
        self._preview.clear()

    def mouse_move(self, pos, modifier=None):
        snapped_pos = self._controller.snap_to_grid(pos)
        self._preview.update_preview(snapped_pos)