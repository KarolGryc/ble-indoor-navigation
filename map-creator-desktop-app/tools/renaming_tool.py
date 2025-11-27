from tools.tool import Tool

from PySide6.QtWidgets import QGraphicsScene
from map_presenter import MapPresenter

from utils.general import ask_zone_name, ask_poi_name_and_type
from model.point_of_interest import PointOfInterest

from model.zone import Zone
from PySide6.QtGui import QTransform

class RenamingTool(Tool):
    def __init__(self,
                 presenter: MapPresenter,
                 scene: QGraphicsScene,
                 name="Zone Renaming Tool"):
        super().__init__(presenter, scene, name)

    def mouse_click(self, pos, modifier=None):
        item = self.scene.itemAt(pos, QTransform())
        item = self.presenter.get_model_for_item(item)

        if item is None:
            return
        
        if isinstance(item, Zone):
            zone: Zone = item
            new_name, zone_type = ask_zone_name("Edit Zone", 
                                               default_name=zone.name,
                                               default_type=zone.type)

            if new_name is None or zone_type is None:
                return
            
            zone.name = new_name.strip()
            zone.type = zone_type

        if isinstance(item, PointOfInterest):
            poi: PointOfInterest = item
            new_name, poi_type = ask_poi_name_and_type(window_name="Edit Point of Interest", 
                                                       default_name=poi.name, 
                                                       default_type=poi.type)

            if new_name is None or poi_type is None:
                return
            
            poi.name = new_name.strip()
            poi.type = poi_type