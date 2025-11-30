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

        self._label = QGraphicsSimpleTextItem(self._zone.name, self)
        self._label.setEnabled(False)

        font = self._label.font()
        font.setPointSize(25)
        font.setBold(True)

        self._label.setBrush(QBrush(QColor("white")))
        self._label.setPen(QPen(QColor("black"), 2))
        self._label.setFont(font)

        self._zone.updated.connect(self.update_item)
        self.update_item()

    def itemChange(self, change, value):
        if change == QGraphicsPolygonItem.ItemSceneHasChanged and value is None:
            try:
                self._zone.updated.disconnect(self.update_item)
            except TypeError:
                pass

        return super().itemChange(change, value)

    def update_item(self):
        self._update_text()
        self._update_geometry()

    def set_highlight(self, highlight=True):
        if highlight:
            self.setBrush(QBrush(QColor(200, 200, 250, 100)))
        else:
            self.setBrush(QBrush(QColor(100, 200, 250, 100)))

    def _update_text(self):
        emote = self.EMOTE_TYPE_MAP.get(self._zone.type, "")
        name = f"{emote} {self._zone.name}" if emote else self._zone.name
        self._label.setText(name)
        self.setToolTip(name)

        text_rect = self._label.boundingRect()
        offset_x = text_rect.width() / 2
        offset_y = text_rect.height() / 2

        pos_x = self._zone.position.x() - offset_x
        pos_y = self._zone.position.y() - offset_y
        self._label.setPos(pos_x, pos_y)

    def _update_geometry(self):
        polygon = QPolygonF([QPointF(node.x, node.y) for node in self._zone.corner_nodes])
        self.setPolygon(polygon)