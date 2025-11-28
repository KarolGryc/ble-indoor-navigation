from PySide6.QtGui import QMouseEvent, QWheelEvent, QPainter 
from PySide6.QtWidgets import QGraphicsView, QGraphicsScene 
from PySide6.QtCore import Qt
from main_map_controller import MainMapController
from view.zoom import GraphicsViewZoom
from view.cursor_modes import *
from widgets.compass import CompassWidget

class MapView(QGraphicsView):
    def __init__(self, presenter: MainMapController):
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

        self._zoom = GraphicsViewZoom(self, min_zoom=0.2, max_zoom=10.0, scale=1.15)

        self._current_rotation = 0.0

        self._compass = CompassWidget(self, scale = 1.25)
        self._update_compass_position()
        self._compass.clicked.connect(self.reset_rotation)

    def resizeEvent(self, event):
        super().resizeEvent(event)
        self._update_compass_position()

    def _update_compass_position(self):
        margin = 20
        x = self.width() - self._compass.width() - margin
        y = margin
        self._compass.move(x, y)

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._last_mouse_pos = event.position()
            event.accept()

        if event.button() == Qt.LeftButton:
            pos = self.mapToScene(event.position().toPoint())
            modifier = event.modifiers()
            self.presenter.on_canvas_click(pos, modifier)
    
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

    def wheelEvent(self, event: QWheelEvent):
        if event.angleDelta().y() > 0:
            self._zoom.zoom_in()
        else:
            self._zoom.zoom_out()

    def keyPressEvent(self, event):
        self.presenter.on_keyboard_press(event.key())
        return super().keyPressEvent(event)

    def mouseDoubleClickEvent(self, event):
        pass

    def rotate(self, angle):
        self._current_rotation += angle
        self._compass.set_angle(self._current_rotation)
        return super().rotate(angle)

    def reset_view(self):
        self.resetTransform()
        self._current_rotation = 0.0
        self._compass.set_angle(self._current_rotation)

    def reset_zoom(self):
        self._zoom.reset_zoom()

    def reset_rotation(self):
        previous_mode = self._transofmation_mode
        self._set_cursor_mode(RotateMode(self))
        self.rotate(-self._current_rotation)
        self._set_cursor_mode(previous_mode)

    def _set_cursor_mode(self, mode: CursorMode):
        if type(self._transofmation_mode) != type(mode):
            self._transofmation_mode = mode