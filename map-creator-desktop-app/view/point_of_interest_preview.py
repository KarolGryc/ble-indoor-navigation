from PySide6.QtCore import Qt
from PySide6.QtGui import QPen, QColor
from PySide6.QtWidgets import QGraphicsScene


class PointOfInterestPreview:
    OPACITY = 0.5
    PEN = QPen(QColor("black"), 2)

    def __init__(self, scene: QGraphicsScene):
        self.scene = scene
        self._poi_preview = None

    def update_preview(self, pos):
        self.clear()

        if pos is not None:
            self._poi_preview = self.scene.addEllipse(
                pos.x() - 5,
                pos.y() - 5,
                10,
                10,
                PointOfInterestPreview.PEN,
            )
            self._poi_preview.setOpacity(self.OPACITY)

    def clear(self):
        if self._poi_preview:
            self.scene.removeItem(self._poi_preview)
            self._poi_preview = None