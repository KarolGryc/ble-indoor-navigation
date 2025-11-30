from __future__ import annotations
from typing import TYPE_CHECKING

from PySide6.QtGui import QTransform

from cad_scene import InteractiveScene

from .tool import Tool
from model import Zone

from view import HighlightPreview
from widgets import ManageZoneConnectionDialog

if TYPE_CHECKING:
    from main_map_controller import MainMapController

class ZoneConnectTool(Tool):
    def __init__(self, 
                 controller: MainMapController, 
                 scene: InteractiveScene, 
                 name="Zone connection management"):
        super().__init__(controller, scene, name)
        self._highlight_preview = HighlightPreview()

    def mouse_click(self, pos, modifier=None):
        item = self._scene.itemAt(pos, QTransform())
        el = self._presenter.get_model_for_item(item)

        if type(el) == Zone:
            parent = self._scene.views()[0] if self._scene.views() else None
            window = ManageZoneConnectionDialog(self._presenter, el, parent)
            window.show()
    
    def mouse_move(self, pos):
        item = self._scene.itemAt(pos, QTransform())

        if type(self._presenter.get_model_for_item(item)) == Zone:
            self._highlight_preview.update_preview(item)
        else:
            self._highlight_preview.clear()