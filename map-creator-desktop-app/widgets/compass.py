from PySide6.QtGui import QPainter, QPen, QBrush, QColor, QPolygonF
from PySide6.QtCore import Qt, QPointF, Signal, QRectF
from PySide6.QtWidgets import QWidget

class CompassWidget(QWidget):
    clicked = Signal()

    BASE_SIZE = 70.0

    def __init__(self, parent=None, scale: float = 1.0):
        super().__init__(parent)
        
        self._scale = scale
        self._angle = 0.0
        self._is_hovered = False

        final_size = int(self.BASE_SIZE * self._scale)
        self.setFixedSize(final_size, final_size)

        self.setAttribute(Qt.WA_Hover)
        self.setCursor(Qt.PointingHandCursor)

    def set_angle(self, angle):
        self._angle = angle
        self.update()

    def enterEvent(self, event):
        self._is_hovered = True
        self.update()
        super().enterEvent(event)

    def leaveEvent(self, event):
        self._is_hovered = False
        self.update()
        super().leaveEvent(event)

    def mouseReleaseEvent(self, event):
        if event.button() == Qt.LeftButton:
            if self.rect().contains(event.position().toPoint()):
                self.clicked.emit()
        super().mouseReleaseEvent(event)

    def paintEvent(self, event):
        painter = QPainter(self)
        painter.setRenderHint(QPainter.Antialiasing)

        painter.scale(self._scale, self._scale)

        cx, cy = self.BASE_SIZE / 2, self.BASE_SIZE / 2
        radius = 25

        # Background
        if self._is_hovered:
            painter.setBrush(QBrush(QColor(200, 200, 200, 200))) 
            painter.setPen(QPen(QColor("gray"), 1))
        else:
            painter.setBrush(QBrush(QColor(255, 255, 255, 150)))
            painter.setPen(Qt.NoPen)

        painter.drawEllipse(QPointF(cx, cy), radius, radius)

        painter.translate(cx, cy)
        painter.rotate(self._angle)

        # North arrow
        painter.setPen(Qt.NoPen)
        painter.setBrush(QColor("#D32F2F"))
        north_arrow = QPolygonF([QPointF(0, -22), QPointF(-5, -2), QPointF(5, -2)])
        painter.drawPolygon(north_arrow)

        # South arrow
        painter.setBrush(QColor("#555")) 
        south_arrow = QPolygonF([QPointF(0, 22), QPointF(-5, 2), QPointF(5, 2)])
        painter.drawPolygon(south_arrow)

        text_color = QColor("black") if self._is_hovered else QColor("#333")
        painter.setPen(text_color)
        
        font = painter.font()
        font.setBold(True)
        font.setPointSize(8)
        painter.setFont(font)

        painter.drawText(QRectF(-10, -34, 20, 10), Qt.AlignCenter, "N")
        painter.drawText(QRectF(-10, 24, 20, 10), Qt.AlignCenter, "S")