from PySide6.QtWidgets import QToolBar, QToolButton, QButtonGroup
from PySide6.QtGui import QIcon
from map_presenter import MapPresenter

from tools.tool import Tool

class Toolbar(QToolBar):
    def __init__(self, 
                 presenter: MapPresenter, 
                 tool_set: list[Tool],
                 tool_icon_map: dict[type, str] = {}):
        super().__init__("Main Toolbar")
        self.presenter = presenter
        
        group = QButtonGroup(self)
        group.setExclusive(True)

        for tool in tool_set:
            button = QToolButton()
            self.addWidget(button)
            button.setText(type(tool).__name__)

            path = tool_icon_map.get(type(tool), None)
            icon = QIcon(path) if path else QIcon()
            button.setIcon(icon)
            button.setToolTip(tool.name)
            button.clicked.connect(lambda checked, t=tool: self._set_current_tool(t))
            group.addButton(button)

    def _set_current_tool(self, tool):
        self.presenter.current_tool = tool

    def _on_current_tool_changed(self, tool):
        # Handle changing style of buttons based on current tool
        pass
    