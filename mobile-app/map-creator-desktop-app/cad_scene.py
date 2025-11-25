import math
from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import Qt, QRectF
from PySide6.QtGui import QPen, QColor, QPainter

class InteractiveScene(QGraphicsScene):
    def __init__(self, presenter = None, background_color=QColor(255, 255, 255)):
        super().__init__()
        self._presenter = presenter
        self.setBackgroundBrush(background_color)

    def set_presenter(self, presenter):
        self._presenter = presenter

    def drawBackground(self, painter: QPainter, rect: QRectF):
        super().drawBackground(painter, rect)
        
        if self._presenter is None:
            return

        grid_size = self._presenter.grid_size

        if not self._presenter.show_grid or grid_size <= 0:
            return
        
        pen = QPen(QColor(50, 50, 50))
        painter.setPen(pen)

        left    = int(math.floor(rect.left() / grid_size) * grid_size)
        right   = int(math.ceil(rect.right() / grid_size) * grid_size)
        top     = int(math.floor(rect.top() / grid_size) * grid_size)
        bottom  = int(math.ceil(rect.bottom() / grid_size) * grid_size)

        for x in range(left, right + 1, grid_size):
            painter.drawLine(x, top, x, bottom)

        for y in range(top, bottom + 1, grid_size):
            painter.drawLine(left, y, right, y)