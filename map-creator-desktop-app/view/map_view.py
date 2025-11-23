from PySide6.QtGui import QMouseEvent, QWheelEvent, QPainter
from PySide6.QtWidgets import QGraphicsView, QGraphicsScene
from PySide6.QtCore import Qt
from map_presenter import MapPresenter
from view.zoom import GraphicsViewZoom

class MapView(QGraphicsView):
    def __init__(self, presenter: MapPresenter):
        super().__init__()
        self.presenter = presenter
        self.scene: QGraphicsScene = presenter.scene
        self.setScene(self.scene)

        self.setRenderHint(QPainter.Antialiasing)
        self.setViewportUpdateMode(QGraphicsView.FullViewportUpdate)
        self.setHorizontalScrollBarPolicy(Qt.ScrollBarAlwaysOff)
        self.setVerticalScrollBarPolicy(Qt.ScrollBarAlwaysOff)

        self.setTransformationAnchor(QGraphicsView.AnchorUnderMouse)
        self.setResizeAnchor(QGraphicsView.AnchorUnderMouse)

        self.setMouseTracking(True)

        self._is_panning = False
        self._pan_start_pos = None

        self._zoom = GraphicsViewZoom(min_zoom=0.2, max_zoom=10.0, scale=1.15)

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._is_panning = True
            self._pan_start_pos = event.position()
            self.setCursor(Qt.ClosedHandCursor)
            return

        pos = self.mapToScene(event.position().toPoint())
        self.presenter.on_canvas_click(pos)
    
    def mouseReleaseEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._is_panning = False
            self.setCursor(Qt.ArrowCursor)
            event.accept()
            return

    def wheelEvent(self, event: QWheelEvent):
        if event.angleDelta().y() > 0:
            self._zoom.zoom_in(self)
        else:
            self._zoom.zoom_out(self)

    def mouseMoveEvent(self, event: QMouseEvent):
        if self._is_panning:
            delta = event.position() - self._pan_start_pos
            self._pan_start_pos = event.position()

            h_bar = self.horizontalScrollBar()
            v_bar = self.verticalScrollBar()
            
            h_bar.setValue(h_bar.value() - delta.x())
            v_bar.setValue(v_bar.value() - delta.y())
            
            event.accept()
            return
        else:
            self.presenter.on_canvas_move(self.mapToScene(event.position().toPoint()))

        super().mouseMoveEvent(event)