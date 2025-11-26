from tools.tool import Tool
from map_presenter import MapPresenter
from PySide6.QtWidgets import QGraphicsScene

from view.node_item import NodeGraphicsItem
from model.node import Node
from PySide6.QtWidgets import QGraphicsScene,  QGraphicsPathItem
from PySide6.QtCore import QPointF, Qt
from PySide6.QtGui import QPainterPath, QPen, QColor

import utils.geometry_utils as geo

class ZoneAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Zone Add Tool"):
        super().__init__(presenter, scene, name)
        self._corner_points = []

        self._preview_polygon = None
        self._preview_point = None

    def deactivate(self):
        self._corner_points = []
        self._cleanup_preview()

    def mouse_click(self, pos, modifier=None):
        pos = self.presenter.snap_to_grid(pos)

        if pos in self._corner_points:
            return
        
        if len(self._corner_points) >= 3:
            last_point = self._corner_points[-1]

            if geo.line_intersects_path(last_point, pos, self._corner_points):
                pen = QPen(QColor("red"), 2)
                if self._preview_polygon:
                    self._preview_polygon.setPen(pen)

                return
        
        self._corner_points.append(pos)

    def mouse_move(self, pos):
        pos = self.presenter.snap_to_grid(pos)

        if self._preview_point is None:
            self._preview_point = NodeGraphicsItem(Node(pos.x(), pos.y()))
            self._preview_point.setOpacity(0.5)
            self.scene.addItem(self._preview_point)
            
        else:
            self._preview_point.setPos(pos)
            self._cleanup_preview_polygon()
            self._draw_preview_polygon()

    def _draw_preview_polygon(self):
        path = QPainterPath()
        points = self._corner_points

        if points:
            path.moveTo(points[0])
             
            for p in points[1:]:
                path.lineTo(p)
            
            if self._preview_point:
                path.lineTo(self._preview_point.pos())

        self._preview_polygon = QGraphicsPathItem(path)

        pen = QPen(QColor("black"), 2)
        self._preview_polygon.setPen(pen)

        self.scene.addItem(self._preview_polygon)

    def _cleanup_preview(self):
        self._cleanup_preview_polygon()
        self._cleanup_preview_point()

    def _cleanup_preview_polygon(self):
        if self._preview_polygon:
            self.scene.removeItem(self._preview_polygon)
            self._preview_polygon = None

    def _cleanup_preview_point(self):
        if self._preview_point:
            self.scene.removeItem(self._preview_point)
            self._preview_point = None