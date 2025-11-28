from tools.tool import Tool
from main_map_controller import MainMapController
from commands.wall_add_command import WallAddCommand
from PySide6.QtWidgets import QGraphicsScene
from view.wall_preview import WallPreview

class WallAddTool(Tool):
    def __init__(self, presenter: MainMapController, scene: QGraphicsScene, name="Wall Add Tool"):
        super().__init__(presenter, scene, name)

        self._start_point = None
        self._preview = WallPreview(scene)

    def deactivate(self):
        self._start_point = None
        self._preview.clear()

    def mouse_click(self, pos, modifier=None):
        pos = self.presenter.snap_to_grid(pos)
        self._preview.update_preview(self._start_point, pos)

        if self._start_point is None:
            self._start_point = pos
        else:
            command = WallAddCommand(self.presenter.current_floor, self._start_point, pos)
            self.presenter.execute(command)
            self.deactivate()

    def mouse_move(self, pos):
        pos = self.presenter.snap_to_grid(pos)
        self._preview.update_preview(self._start_point, pos)