from tools.tool import Tool

from main_map_controller import MainMapController
from PySide6.QtWidgets import QGraphicsScene

from commands.point_of_interest_add_command import PointOfInterestAddCommand
from view.point_of_interest_preview import PointOfInterestPreview
from utils.general import ask_poi_name_and_type

class PointOfInterestAddTool(Tool):
    def __init__(self, 
                 presenter: MainMapController, 
                 scene: QGraphicsScene, 
                 name="Add Place"):
        super().__init__(presenter, scene, name)
        self._preview = PointOfInterestPreview(scene)

    def deactivate(self):
        self._preview.clear()
        return super().deactivate()

    def mouse_click(self, pos, modifier=None):
        snapped_pos = self.presenter.snap_to_grid(pos)

        name, poi_type = ask_poi_name_and_type(window_name="Enter Point Details")

        if name is None or poi_type is None:
            return

        cmd = PointOfInterestAddCommand(self.presenter.current_floor, snapped_pos, name, poi_type)
        self.presenter.execute(cmd)
        self._preview.clear()

    def mouse_move(self, pos, modifier=None):
        snapped_pos = self.presenter.snap_to_grid(pos)
        self._preview.update_preview(snapped_pos)