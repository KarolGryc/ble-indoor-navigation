from tools.tool import Tool
from PySide6.QtWidgets import QGraphicsScene, QInputDialog
from PySide6.QtCore import QPointF
from view.zone_preview import ZonePreview
from map_presenter import MapPresenter
from view.node_item import NodeGraphicsItem
from model.node import Node
from commands.zone_add_command import ZoneAddCommand

import utils.geometry_utils as geo


class ZoneAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Zone Add Tool"):
        super().__init__(presenter, scene, name)
        self._corner_points = []

        self._preview = ZonePreview(scene)

    def deactivate(self):
        self._corner_points = []
        self._preview.clear()

    def mouse_click(self, pos, modifier=None):
        pos = self.presenter.snap_to_grid(pos)
        
        if not self._is_polygon_valid(pos):
            return
        
        if len(self._corner_points) == 0 or pos != self._corner_points[0]:
            self._corner_points.append(pos)
        else:
            name, ok = QInputDialog.getText(
                None, "Enter zone name", "Zone Name:", text="Example Zone"
            )
            if not ok or name.strip() == "":
                return
            
            cmd = ZoneAddCommand(self.presenter.model, self._corner_points, name.strip())
            self.presenter.execute(cmd)
            self.deactivate()

    def mouse_move(self, pos):
        pos = self.presenter.snap_to_grid(pos)
        self._preview.update_preview(self._corner_points, pos, self._is_polygon_valid(pos))

    def _is_polygon_valid(self, new_point: QPointF) -> bool:
        if len(self._corner_points) < 1:
            return True

        intersection = geo.get_self_intersetion(self._corner_points + [new_point])
        if intersection == self._corner_points[0] and len(self._corner_points) >= 3 and new_point == self._corner_points[0]:
            return True
        return intersection is None
