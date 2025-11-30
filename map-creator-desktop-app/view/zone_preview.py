from PySide6.QtGui import QPainterPath, QPen, QColor
from PySide6.QtCore import QPointF
from PySide6.QtWidgets import QGraphicsScene, QGraphicsPathItem

import shiboken6

from model import Node
from .node_item import NodeGraphicsItem

class ZonePreview:
    DEFAULT_PEN = QPen(QColor("black"), 2)
    ERROR_PEN = QPen(QColor("red"), 2)

    def __init__(self, scene: QGraphicsScene):
        self.scene = scene
        self._polygon_item = None
        self._point_item = None
        self._current_pen = self.DEFAULT_PEN

    def update_preview(self, corner_points: list[QPointF], new_point: QPointF, valid: bool):
        self.clear()

        path = self._create_path(corner_points, new_point)

        self._polygon_item = QGraphicsPathItem(path)
        self._polygon_item.setPen(self.DEFAULT_PEN if valid else self.ERROR_PEN)
        self.scene.addItem(self._polygon_item)

        self._point_item = NodeGraphicsItem(Node(new_point.x(), new_point.y()))
        self._point_item.setOpacity(0.5)
        self.scene.addItem(self._point_item)

    def clear(self):
        if self._polygon_item and shiboken6.isValid(self._polygon_item):
            self.scene.removeItem(self._polygon_item)
            self._polygon_item = None

        if self._point_item and shiboken6.isValid(self._point_item):
            self.scene.removeItem(self._point_item)
            self._point_item = None

    def _create_path(self, corner_points: list[QPointF], new_point: QPointF) -> QPainterPath:
        path = QPainterPath()
        if corner_points:
            path.moveTo(corner_points[0])

            for point in corner_points[1:]:
                path.lineTo(point)
            path.lineTo(new_point)
        return path