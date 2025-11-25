import math
from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import Qt, QRectF
from PySide6.QtGui import QPen, QColor, QPainter

class InteractiveScene(QGraphicsScene):
    def __init__(self, presenter=None, background_color=QColor(255, 255, 255)):
        super().__init__()
        self._presenter = presenter
        self.setBackgroundBrush(background_color)
        
        self._grid_color = QColor(220, 220, 220)
        self._axis_color = QColor(80, 80, 80)
        self._text_color = QColor(50, 50, 50)

    def set_presenter(self, presenter):
        self._presenter = presenter

    def drawBackground(self, painter: QPainter, rect: QRectF):
        super().drawBackground(painter, rect)
        
        if self._presenter is None or not self._presenter.show_grid:
            return

        grid_size = self._presenter.grid_size
        if grid_size <= 0: return

        grid_pen = QPen(self._grid_color, 0)
        axis_pen = QPen(self._axis_color, 0)

        font = painter.font()
        font.setPixelSize(int(grid_size * 0.25)) 
        painter.setFont(font)

        left    = int(math.floor(rect.left() / grid_size) * grid_size)
        right   = int(math.ceil(rect.right() / grid_size) * grid_size)
        top     = int(math.floor(rect.top() / grid_size) * grid_size)
        bottom  = int(math.ceil(rect.bottom() / grid_size) * grid_size)

        painter.setPen(grid_pen)

        for x in range(left, right + 1, int(grid_size)):
            if x == 0: continue
            painter.drawLine(x, top, x, bottom)

        for y in range(top, bottom + 1, int(grid_size)):
            if y == 0: continue
            painter.drawLine(left, y, right, y)

        painter.setPen(axis_pen)
        
        if left <= 0 <= right:
            painter.drawLine(0, top, 0, bottom)
            
            for y in range(top, bottom + 1, int(grid_size)):
                if y == 0: continue
                label = str(-y / 100) + 'm'
                painter.drawText(QRectF(-grid_size, y, grid_size - 2, grid_size), 
                                 Qt.AlignRight | Qt.AlignTop, label)

        if top <= 0 <= bottom:
            painter.drawLine(left, 0, right, 0)
            
            for x in range(left, right + 1, int(grid_size)):
                if x == 0: continue
                label = str(x / 100) + 'm'
                painter.drawText(QRectF(x, 2, grid_size, grid_size), 
                                 Qt.AlignLeft | Qt.AlignTop, label)