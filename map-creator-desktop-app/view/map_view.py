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
        self._is_rotating = False
        self._last_mouse_pos = None 

        self._zoom = GraphicsViewZoom(min_zoom=0.2, max_zoom=10.0, scale=1.15)

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._last_mouse_pos = event.position()
            if event.modifiers() & Qt.AltModifier: # Rotation
                self._is_rotating = True
                self._is_panning = False
                self.setCursor(Qt.SizeAllCursor)
            else: # Panning
                self._is_panning = True
                self._is_rotating = False
                self.setCursor(Qt.ClosedHandCursor)
    
            event.accept()

        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.position().toPoint())
            modifier = event.modifiers()
            self.presenter.on_canvas_click(pos, modifier)
            super().mousePressEvent(event)
    

    def mouseMoveEvent(self, event: QMouseEvent):
        if self._is_panning or self._is_rotating:
            delta = event.position() - self._last_mouse_pos
            self._last_mouse_pos = event.position()

            if self._is_rotating:
                self.setTransformationAnchor(QGraphicsView.AnchorViewCenter)
                angle = delta.x() * 0.5 
                self.rotate(angle)
            
            elif self._is_panning:
                self.setTransformationAnchor(QGraphicsView.AnchorUnderMouse)
                h_bar = self.horizontalScrollBar()
                v_bar = self.verticalScrollBar()
                h_bar.setValue(h_bar.value() - delta.x())
                v_bar.setValue(v_bar.value() - delta.y())
            
            event.accept()
            return
        
        else:
            self.presenter.on_canvas_move(self.mapToScene(event.position().toPoint()))

    def mouseReleaseEvent(self, event: QMouseEvent):
        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.position().toPoint())
            self.presenter.on_canvas_release(pos)
            event.accept()

        if event.button() == Qt.MiddleButton:
            self._is_panning = False
            self._is_rotating = False
            self.setCursor(Qt.ArrowCursor)
            event.accept()

    def wheelEvent(self, event: QWheelEvent):
        if event.angleDelta().y() > 0:
            self._zoom.zoom_in(self)
        else:
            self._zoom.zoom_out(self)

    def keyPressEvent(self, event):
        key = event.key()
        self.presenter.on_keyboard_button_press(key)