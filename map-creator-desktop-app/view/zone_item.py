from model.zone import Zone
from PySide6.QtWidgets import QGraphicsPolygonItem
from PySide6.QtGui import QBrush, QPen, QColor, QPolygonF
from PySide6.QtCore import QPointF
from PySide6.QtWidgets import QGraphicsSimpleTextItem

class ZoneGraphicsItem(QGraphicsPolygonItem):
    def __init__(self, zone: Zone):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in zone.corner_nodes])   
        super().__init__(polygon)

        self._zone = zone

        self.setBrush(QBrush(QColor(100, 200, 250, 100)))
        self.setPen(QPen(QColor("blue"), 2))

        self.setFlag(QGraphicsPolygonItem.ItemIsSelectable, True)
        self.setToolTip(self._zone.name)

        self._text_item = QGraphicsSimpleTextItem(self._zone.name, self)

        font = self._text_item.font()
        font.setPointSize(25)
        font.setBold(True)

        self._text_item.setBrush(QBrush(QColor("white")))
        self._text_item.setPen(QPen(QColor("black"), 2))
        self._text_item.setFont(font)

        self.update_geometry()

    def update(self):
        self.update_text()
        self.update_geometry()

    def update_text(self):
        self._text_item.setText(self._zone.name)
        self.setToolTip(self._zone.name)

    def update_geometry(self):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in self._zone.corner_nodes])
        self.setPolygon(polygon)

        text_rect = self._text_item.boundingRect()
        offset_x = text_rect.width() / 2
        offset_y = text_rect.height() / 2
        self._text_item.setPos(self._zone.position.x() - offset_x, self._zone.position.y() - offset_y)