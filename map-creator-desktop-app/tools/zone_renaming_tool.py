from tools.tool import Tool

from PySide6.QtWidgets import QGraphicsScene
from map_presenter import MapPresenter

from utils.general import ask_zone_name

from model.zone import Zone
from PySide6.QtGui import QTransform

class ZoneRenamingTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Zone Renaming Tool"):
        super().__init__(presenter, scene, name)

    def mouse_click(self, pos, modifier=None):
        item = self.scene.itemAt(pos, QTransform())
        item = self.presenter.get_model_for_item(item)

        if item is None or not isinstance(item, Zone):
            return
        
        zone: Zone = item
        new_name = ask_zone_name(zone.name)

        if new_name is None:
            return
        
        zone.name = new_name.strip()