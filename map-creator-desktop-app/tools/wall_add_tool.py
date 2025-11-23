from tools.tool import Tool
from model.wall import Wall
from model.node import Node
from map_presenter import MapPresenter
from commands.wall_add_command import WallAddCommand
from PySide6.QtWidgets import QGraphicsScene


class WallAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Wall Add Tool"):
        super().__init__(presenter, scene, name)

        self._first_click_pos = None
        self._click_point_preview = None
        self._preview_line = None

    def mouse_click(self, pos):
        if self._first_click_pos is None:
            self._first_click_pos = pos
            self._click_point_preview = self.scene.addEllipse(
                pos.x() - 3, pos.y() - 3, 6, 6,
            )
            self._preview_line = self.scene.addLine(
                pos.x(), pos.y(), pos.x(), pos.y()
            )
        else:
            command = WallAddCommand(self.presenter.model, self._first_click_pos, pos)
            self.presenter.execute_command(command)
            self._reset_tool()

    def _reset_tool(self):
        self._first_click_pos = None
        self._cleanup_preview()

    def _cleanup_preview(self):
        if self._click_point_preview:
            self.scene.removeItem(self._click_point_preview)
            self._click_point_preview = None

        if self._preview_line:
            self.scene.removeItem(self._preview_line)
            self._preview_line = None

    def mouse_move(self, pos):
        if self._first_click_pos is not None and self._preview_line is not None:
            self._preview_line.setLine(
                self._first_click_pos.x(),
                self._first_click_pos.y(),
                pos.x(),
                pos.y()
            )