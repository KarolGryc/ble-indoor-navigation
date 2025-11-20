from model import Point, BuildingMap
from PySide6.QtCore import Signal, QObject
from tools import *

class BuildingViewModel(QObject):
    activeToolChanged = Signal(object)

    def __init__(self):
        super().__init__()
        self.map = BuildingMap()
        
        self.tools = [
            WallTool(),
            SelectTool()
        ]

        self._active_tool = self.tools[0]

    @property
    def active_tool(self):
        return self._active_tool
    
    @active_tool.setter
    def active_tool(self, tool):
        if tool is not self._active_tool:
            self._active_tool = tool
            self.activeToolChanged.emit(tool)