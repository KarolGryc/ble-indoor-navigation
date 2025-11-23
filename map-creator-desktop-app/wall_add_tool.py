from tool import Tool
from wall import Wall
from node import Node
from map_presenter import MapPresenter
from wall_add_command import WallAddCommand
from PySide6.QtWidgets import QGraphicsScene


class WallAddTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene):
        self.presenter = presenter
        self.scene = scene
        self.first_click = None

        self._click_point_preview = None
        self._preview_line = None

    def mouse_click(self, pos):
        if self.first_click is None:
            self.first_click = pos
            self._click_point_preview = self.scene.addEllipse(
                pos.x() - 3, pos.y() - 3, 6, 6,
            )
            self._preview_line = self.scene.addLine(
                pos.x(), pos.y(), pos.x(), pos.y()
            )
        else:
            command = WallAddCommand(
                self.presenter.model,
                self.first_click,
                pos
            )
            self.presenter.undo_stack.push(command)
            self.first_click = None

            # Cleanup preview
            if self._click_point_preview:
                self.scene.removeItem(self._click_point_preview)
                self._click_point_preview = None
            if self._preview_line:
                self.scene.removeItem(self._preview_line)
                self._preview_line = None

    def mouse_move(self, pos):
        if self.first_click is not None and self._preview_line is not None:
            self._preview_line.setLine(
                self.first_click.x(),
                self.first_click.y(),
                pos.x(),
                pos.y()
            )