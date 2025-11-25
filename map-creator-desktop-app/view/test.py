from PySide6.QtGui import QMouseEvent, QWheelEvent, QPainter 
from PySide6.QtWidgets import QGraphicsView, QGraphicsScene 
from PySide6.QtCore import Qt
from map_presenter import MapPresenter
from view.zoom import GraphicsViewZoom
from view.cursor_modes import *

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

        self._last_mouse_pos = None
        self._transofmation_mode: CursorMode = NormalMode(self)

        self._zoom = GraphicsViewZoom(min_zoom=0.2, max_zoom=10.0, scale=1.15)

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._last_mouse_pos = event.position()
            event.accept()

        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.position().toPoint())
            modifier = event.modifiers()
            self.presenter.on_canvas_click(pos, modifier)
            
        super().mousePressEvent(event)
    

    def mouseMoveEvent(self, event: QMouseEvent):
        canva_pos = event.position()
        if event.buttons() & Qt.MiddleButton:
            delta = canva_pos - self._last_mouse_pos
            self._last_mouse_pos = canva_pos

            modifier = event.modifiers()
            if modifier == Qt.AltModifier:
                self._set_cursor_mode(RotateMode(self))
            else:
                self._set_cursor_mode(PanMode(self))
            
            self._transofmation_mode.apply_transformation(delta)
            event.accept()
        
        else:
            self._set_cursor_mode(NormalMode(self))
            mapped_pos = self.mapToScene(canva_pos.toPoint())
            self.presenter.on_canvas_move(mapped_pos)

        super().mouseMoveEvent(event)

    def mouseReleaseEvent(self, event: QMouseEvent):
        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.position().toPoint())
            self.presenter.on_canvas_release(pos)
            event.accept()

        if event.button() == Qt.MiddleButton:
            self._set_cursor_mode(NormalMode(self))
            event.accept()

        super().mouseReleaseEvent(event)

    def wheelEvent(self, event: QWheelEvent):
        if event.angleDelta().y() > 0:
            self._zoom.zoom_in(self)
        else:
            self._zoom.zoom_out(self)

        super().wheelEvent(event)

    def _set_cursor_mode(self, mode: CursorMode):
        if type(self._transofmation_mode) != type(mode):
            self._transofmation_mode = mode
            print(f"Cursor mode changed to: {type(mode).__name__}")