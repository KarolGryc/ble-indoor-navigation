from PySide6.QtWidgets import QGraphicsView, QGraphicsScene
from PySide6.QtGui import QPen, QMouseEvent, QWheelEvent, QPainter
from PySide6.QtCore import Qt

class MapView(QGraphicsView):
    def __init__(self, viewmodel):
        super().__init__()
        self.viewmodel = viewmodel

        self.scene = QGraphicsScene()
        self.setScene(self.scene)

        # Parametry zoomowania
        self.zoom_factor = 1.15
        self.min_zoom = 0.1
        self.max_zoom = 10

        # Tryb przesuwania widoku
        self.is_panning = False
        self.last_pan_pos = None

        # Ustawienia jakości
        self.setRenderHint(QPainter.Antialiasing)
        self.setDragMode(QGraphicsView.NoDrag)
        self.setTransformationAnchor(QGraphicsView.AnchorUnderMouse)

    # -------------------------------
    # RYSOWANIE ŚCIAN
    # -------------------------------
    def draw_wall(self, x1, y1, x2, y2):
        pen = QPen(Qt.black, 2)
        self.scene.addLine(x1, y1, x2, y2, pen)

    # -------------------------------
    # KLIK LEWYM PRZYCISKIEM – RYSOWANIE
    # -------------------------------
    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.pos())
            result = self.viewmodel.click(pos.x(), pos.y())
            if result:
                start, end = result
                self.draw_wall(start.x, start.y, end.x, end.y)

        elif event.button() == Qt.MiddleButton:
            # Zaczynamy przesuwanie
            self.is_panning = True
            self.last_pan_pos = event.pos()
            self.setCursor(Qt.ClosedHandCursor)

        super().mousePressEvent(event)

    def mouseMoveEvent(self, event: QMouseEvent):
        if self.is_panning:
            # Różnica pozycji myszy
            delta = event.pos() - self.last_pan_pos
            self.last_pan_pos = event.pos()

            # Przesuwanie widoku
            self.horizontalScrollBar().setValue(
                self.horizontalScrollBar().value() - delta.x()
            )
            self.verticalScrollBar().setValue(
                self.verticalScrollBar().value() - delta.y()
            )
        else:
            super().mouseMoveEvent(event)

    def mouseReleaseEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self.is_panning = False
            self.setCursor(Qt.ArrowCursor)
        super().mouseReleaseEvent(event)

    # -------------------------------
    # ZOOM SCROLLEM MYSZY
    # -------------------------------
    def wheelEvent(self, event: QWheelEvent):
        # delta > 0 -> zoom in
        # delta < 0 -> zoom out
        zoom_in = event.angleDelta().y() > 0

        # Obecna skala
        current_scale = self.transform().m11()

        if zoom_in and current_scale < self.max_zoom:
            scale_factor = self.zoom_factor
        elif not zoom_in and current_scale > self.min_zoom:
            scale_factor = 1 / self.zoom_factor
        else:
            return  # ignoruj zoom poza limitem

        self.scale(scale_factor, scale_factor)
