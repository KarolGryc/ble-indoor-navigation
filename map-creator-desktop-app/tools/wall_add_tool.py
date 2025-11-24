from tools.tool import Tool
from map_presenter import MapPresenter
from commands.wall_add_command import WallAddCommand
from PySide6.QtWidgets import QGraphicsScene


class WallAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Wall Add Tool"):
        super().__init__(presenter, scene, name)

        self._start_point = None

        # Preview items
        self._start_point_preview = None
        self._preview_line = None
        self._end_point_preview = None

    def deactivate(self):
        self._start_point = None
        self._cleanup_preview()

    def mouse_click(self, pos):
        pos = self.presenter.snap_to_grid(pos)
        if self._start_point is None:
            self._start_point = pos
            self._start_point_preview = self.scene.addEllipse(
                pos.x() - 3, pos.y() - 3, 6, 6,
            )
            self._preview_line = self.scene.addLine(
                pos.x(), pos.y(), pos.x(), pos.y()
            )
            self._end_point_preview = self.scene.addEllipse(
                pos.x() - 3, pos.y() - 3, 6, 6,
            )
        else:
            command = WallAddCommand(self.presenter.model, self._start_point, pos)
            self.presenter.execute(command)
            self.deactivate()

    def mouse_move(self, pos):
        pos = self.presenter.snap_to_grid(pos)
        if self._preview_line is not None:    
            self._preview_line.setLine(
                self._start_point.x(),
                self._start_point.y(),
                pos.x(),
                pos.y()
            )

        if self._end_point_preview is not None:
            self._end_point_preview.setRect(
                pos.x() - 3, pos.y() - 3, 6, 6,
            )

    def _cleanup_preview(self):
        if self._start_point_preview:
            self.scene.removeItem(self._start_point_preview)
            self._start_point_preview = None

        if self._preview_line:
            self.scene.removeItem(self._preview_line)
            self._preview_line = None

        if self._end_point_preview:
            self.scene.removeItem(self._end_point_preview)
            self._end_point_preview = None