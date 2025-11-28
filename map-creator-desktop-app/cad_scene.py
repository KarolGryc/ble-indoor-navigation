import math
from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtCore import Qt, QRectF, QPointF
from PySide6.QtGui import QPen, QColor, QPainter

from main_map_controller import MainMapController

class InteractiveScene(QGraphicsScene):
    ACTIVE_OPACITY = 1.0
    INACTIVE_OPACITY = 0.3

    def __init__(self, presenter:MainMapController=None, background_color=QColor(255, 255, 255)):
        super().__init__()
        self._presenter = presenter
        self.setBackgroundBrush(background_color)
        
        self._grid_color = QColor(220, 220, 220)
        self._axis_color = QColor(80, 80, 80)
        self._text_color = QColor(50, 50, 50)

        self._active_type = None

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

    def addItem(self, item):
        super().addItem(item)    
        
        model = self._presenter.get_model_for_item(item)
        if model is None:
            return

        if model is not None:
            item_type = type(model)
            is_active = self._active_type is None or item_type == self._active_type
            self._set_active_state(item, is_active)

            for dep in model.dependencies:
                dep_item = self._presenter.get_item_for_model(dep)
                self._set_active_state(dep_item, is_active)

    def itemAt(self, pos: QPointF, transform):
        items = self.items(pos, Qt.IntersectsItemShape, Qt.DescendingOrder)
        for item in items:
            if item.isEnabled():
                return item
            
        return None
                
    def set_active_item_type(self, item_type: type):
        self._active_type = item_type

        checked = set()

        for item in self.items():
            model = self._presenter.get_model_for_item(item)
            if model is None or item in checked:
                continue

            is_active = type(model) == self._active_type
            self._set_active_state(item, is_active)
            checked.add(item)

            for dep in model.dependencies:
                dep_item = self._presenter.get_item_for_model(dep)
                self._set_active_state(dep_item, is_active)
                checked.add(dep_item)

    def _set_active_state(self, item, is_active: bool):
        if item is None:
            return
        
        opacity = InteractiveScene.ACTIVE_OPACITY if is_active else InteractiveScene.INACTIVE_OPACITY
        item.setEnabled(is_active)
        item.setOpacity(opacity)

        if not is_active:
            item.setSelected(False)