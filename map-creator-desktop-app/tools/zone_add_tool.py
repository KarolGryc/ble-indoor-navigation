from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtCore import QPointF
from PySide6.QtWidgets import QGraphicsScene

from utils.general import ask_zone_name
import utils.geometry_utils as geo

from .tool import Tool
from view import ZonePreview
from commands import ZoneAddCommand

if TYPE_CHECKING:
    from main_map_controller import MainMapController

class ZoneAddTool(Tool):
    def __init__(self, presenter: MainMapController, scene: QGraphicsScene, name="Add Zone"):
        super().__init__(presenter, scene, name)
        self._corner_points = []

        self._preview = ZonePreview(scene)

    def deactivate(self):
        self._corner_points = []
        self._preview.clear()

    def mouse_click(self, pos, modifier=None):
        pos = self._controller.snap_to_grid(pos)
        
        if not self._is_polygon_valid(pos):
            return
        
        if len(self._corner_points) == 0 or pos != self._corner_points[0]:
            self._corner_points.append(pos)
        else:
            name, zone_type = ask_zone_name("New Zone")
            if name is None or zone_type is None:
                return

            cmd = ZoneAddCommand(self._controller.current_floor, self._corner_points, name.strip(), zone_type)
            self._controller.execute(cmd)
            self.deactivate()

    def mouse_move(self, pos):
        pos = self._controller.snap_to_grid(pos)
        self._preview.update_preview(self._corner_points, pos, self._is_polygon_valid(pos))

    def _is_polygon_valid(self, new_point: QPointF) -> bool:
        if len(self._corner_points) < 1:
            return True

        start = self._corner_points[0]
        intersection = geo.get_self_intersetion(self._corner_points + [new_point])
        if intersection == start and len(self._corner_points) >= 3 and new_point == start:
            return True
        return intersection is None
