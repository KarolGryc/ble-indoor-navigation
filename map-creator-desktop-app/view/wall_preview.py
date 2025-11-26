from PySide6.QtCore import QPointF
from PySide6.QtWidgets import QGraphicsScene
from view.node_item import NodeGraphicsItem
from model.node import Node

class WallPreview:
    def __init__(self, scene: QGraphicsScene):
        self.scene = scene
        self._start_preview = None
        self._line_preview = None
        self._end_preview = None

    def update_preview(self, start_pos: QPointF, cursor_pos: QPointF):
        self.clear()

        if start_pos is None:
            self._start_preview = NodeGraphicsItem(Node(cursor_pos.x(), cursor_pos.y()))
            self._start_preview.setOpacity(0.5)
            self.scene.addItem(self._start_preview)
        else:
            self._start_preview = NodeGraphicsItem(Node(start_pos.x(), start_pos.y()))
            self._start_preview.setOpacity(0.5)
            self.scene.addItem(self._start_preview)

            self._line_preview = self.scene.addLine(
                start_pos.x(), start_pos.y(), cursor_pos.x(), cursor_pos.y()
            )
            self._line_preview.setOpacity(0.5)

            self._end_preview = NodeGraphicsItem(Node(cursor_pos.x(), cursor_pos.y()))
            self._end_preview.setOpacity(0.5)
            self.scene.addItem(self._end_preview)

    def clear(self):
        if self._start_preview:
            self.scene.removeItem(self._start_preview)
            self._start_preview = None

        if self._line_preview:
            self.scene.removeItem(self._line_preview)
            self._line_preview = None

        if self._end_preview:
            self.scene.removeItem(self._end_preview)
            self._end_preview = None