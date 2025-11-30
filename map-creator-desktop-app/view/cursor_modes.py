from PySide6.QtCore import QPointF, Qt
from PySide6.QtWidgets import QGraphicsView

from abc import ABC, abstractmethod

class CursorMode(ABC):
    def __init__(self, view: QGraphicsView):
        self._view = view

    @abstractmethod
    def apply_transformation(self, delta: QPointF):
        pass

class NormalMode(CursorMode):
    def __init__(self, view: QGraphicsView):
        super().__init__(view)
        self._view.setCursor(Qt.ArrowCursor)
        self._view.setTransformationAnchor(QGraphicsView.AnchorUnderMouse)

    def apply_transformation(self, delta: QPointF):
        pass

class PanMode(CursorMode):
    def __init__(self, view: QGraphicsView):
        super().__init__(view)
        self._view.setCursor(Qt.ClosedHandCursor)
        self._view.setTransformationAnchor(QGraphicsView.AnchorUnderMouse)

    def apply_transformation(self, delta: QPointF):
        h_bar = self._view.horizontalScrollBar()
        v_bar = self._view.verticalScrollBar()
        h_bar.setValue(h_bar.value() - delta.x())
        v_bar.setValue(v_bar.value() - delta.y())

class RotateMode(CursorMode):
    def __init__(self, view: QGraphicsView, rotation_speed: float = 0.5):
        super().__init__(view)
        self._rotation_speed = rotation_speed
        self._view.setCursor(Qt.SizeAllCursor)
        self._view.setTransformationAnchor(QGraphicsView.AnchorViewCenter)

    def apply_transformation(self, delta: QPointF):
        angle = delta.x() * self._rotation_speed
        self._view.rotate(angle)