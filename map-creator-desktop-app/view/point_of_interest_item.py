from model.point_of_interest import PointOfInterestType
from PySide6.QtWidgets import QGraphicsPathItem, QGraphicsPixmapItem, QGraphicsSimpleTextItem
from PySide6.QtGui import QPainterPath, QBrush, QColor, QPen, QPixmap, QPainter
from PySide6.QtCore import Qt

class PointOfInterestGraphicsItem(QGraphicsPathItem):
    type_to_image = {
            PointOfInterestType.GENERIC: "icons/generic.png",
            PointOfInterestType.RESTAURANT: "icons/restaurant.png",
            PointOfInterestType.SHOP: "icons/shop.png",
            PointOfInterestType.TOILET: "icons/toilet.png",
            PointOfInterestType.EXIT: "icons/exit.png",
    }

    type_to_color = {
            PointOfInterestType.GENERIC: "#D32C2C",
            PointOfInterestType.RESTAURANT: "#F57C00",
            PointOfInterestType.SHOP: "#FBC02D",
            PointOfInterestType.TOILET: "#1976D2",
            PointOfInterestType.EXIT: "#388E3C",
    }

    def __init__(self, point_of_interest):
        super().__init__()
        self._point_of_interest = point_of_interest

        self._icon_path = self._get_pin_path()

        self._text_item = QGraphicsSimpleTextItem(self._point_of_interest.name, parent=self)
        
        self._avatar_item = None

        font = self._text_item.font()
        font.setPointSize(25)
        font.setBold(True)

        self._text_item.setFont(font)
        self._text_item.setBrush(QBrush(Qt.white))
        self._text_item.setPen(QPen(Qt.black, 2))

        self.setFlag(QGraphicsPathItem.ItemIsSelectable, True)
        self.setZValue(2)

        self._point_of_interest.updated.connect(self.update_item)
        
        self.update_item()

    def itemChange(self, change, value):
        if change == QGraphicsPathItem.ItemSceneHasChanged and value is None:
            try:
                self._point_of_interest.updated.disconnect(self.update_item)
            except TypeError:
                pass

        return super().itemChange(change, value)


    def update_item(self):
        self._update_label()
        self._update_pin()
        self._update_geometry()
        self.setPos(self._point_of_interest.position)

    def _update_geometry(self):
        text_rect = self._text_item.boundingRect()
        offset_x = 10
        offset_y = -text_rect.height() / 2
        self._text_item.setPos(offset_x, offset_y)

    def _update_pin(self):
        self.setPath(self._icon_path)

        color = self._get_coloring()
        self.setBrush(QBrush(color))
        self.setPen(QPen(Qt.black, 1))

        render_size = 64
        original_pixmap = self._get_image_pixmap(render_size)
        original_pixmap = original_pixmap.scaled(render_size, 
                                                 render_size, 
                                                 Qt.KeepAspectRatioByExpanding, 
                                                 Qt.SmoothTransformation)
        
        circular_pixmap = QPixmap(render_size, render_size)
        circular_pixmap.fill(Qt.transparent)
        
        painter = QPainter(circular_pixmap)
        painter.setRenderHint(QPainter.Antialiasing)
        painter.setRenderHint(QPainter.SmoothPixmapTransform)
        
        path_circle = QPainterPath()
        path_circle.addEllipse(0, 0, render_size, render_size)
        painter.setClipPath(path_circle)
        
        painter.drawPixmap(0, 0, original_pixmap)
        painter.end()

        if self._avatar_item:
            self._avatar_item.setPixmap(circular_pixmap)
        else:
            self._avatar_item = QGraphicsPixmapItem(circular_pixmap, parent=self)
            self._avatar_item.setTransformationMode(Qt.SmoothTransformation)
            self._avatar_item.setScale(0.5)
        
            center_y_of_head = -40
            real_width = self._avatar_item.pixmap().width() * self._avatar_item.scale()
            self._avatar_item.setPos(-real_width / 2, center_y_of_head - (real_width / 2))

        self.setScale(0.6)

    def _update_label(self):
        self._text_item.setText(self._point_of_interest.name)
        self.setToolTip(self._point_of_interest.name)

    def _get_coloring(self) -> QColor:
        color = PointOfInterestGraphicsItem.type_to_color.get(
            self._point_of_interest.type, "#D32C2C"
        )
        return QColor(color)

    def _get_image_pixmap(self, default_size=64) -> QPixmap:
        path = PointOfInterestGraphicsItem.type_to_image.get(
            self._point_of_interest.type, "icons/generic.png"
        )

        pixmap = QPixmap(path)
        if pixmap.isNull():
            pixmap = QPixmap(default_size, default_size)
            pixmap.fill(Qt.white)

        return pixmap
    
    def _get_pin_path(self) -> QPainterPath:
        path = QPainterPath()
        path.cubicTo(-8, -12, -20, -28, -20, -40)   # Left side
        path.cubicTo(-20, -67, 20, -67, 20, -40)    # Top curve
        path.cubicTo(20, -28, 8, -12, 0, 0)         # Right side

        return path