from model.zone import Zone
from PySide6.QtWidgets import QGraphicsPolygonItem
from PySide6.QtGui import QBrush, QPen, QColor, QPolygonF
from PySide6.QtCore import QPointF
from PySide6.QtWidgets import QGraphicsSimpleTextItem

from model.zone import ZoneType

class ZoneGraphicsItem(QGraphicsPolygonItem):
    EMOTE_TYPE_MAP = {
        ZoneType.GENERIC: "",
        ZoneType.STAIRS: "𓊍",
        ZoneType.ELEVATOR: "↕",
    }

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

        self.update_item()

    def update_item(self):
        self._update_text()
        self._update_geometry()

    def _update_text(self):
        emote = self.EMOTE_TYPE_MAP.get(self._zone.type, "")
        name = f"{emote} {self._zone.name}" if emote else self._zone.name
        self._text_item.setText(name)
        self.setToolTip(name)

    def _update_geometry(self):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in self._zone.corner_nodes])
        self.setPolygon(polygon)

        text_rect = self._text_item.boundingRect()
        offset_x = text_rect.width() / 2
        offset_y = text_rect.height() / 2
        self._text_item.setPos(self._zone.position.x() - offset_x, self._zone.position.y() - offset_y)