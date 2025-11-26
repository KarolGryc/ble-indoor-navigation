from model.zone import Zone
from PySide6.QtWidgets import QGraphicsPolygonItem
from PySide6.QtGui import QBrush, QPen, QColor, QPolygonF
from PySide6.QtCore import QPointF

class ZoneGraphicsItem(QGraphicsPolygonItem):
    def __init__(self, zone: Zone):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in zone.corner_nodes])   
        super().__init__(polygon)
        self.zone = zone
        brush = QBrush(QColor(100, 200, 250, 100))
        pen = QPen(QColor("blue"), 2)
        self.setBrush(brush)
        self.setPen(pen)


    def update_geometry(self):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in self.zone.corner_nodes])
        self.setPolygon(polygon)