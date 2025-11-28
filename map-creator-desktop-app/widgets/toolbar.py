from PySide6.QtWidgets import QToolBar, QToolButton, QButtonGroup
from PySide6.QtGui import QIcon, QPixmap, QColor
from main_map_controller import MainMapController

from tools.tool import Tool

from utils.general import _is_dark_theme

class Toolbar(QToolBar):
    def __init__(self, 
                 presenter: MainMapController, 
                 tool_set: list[Tool],
                 tool_icon_map: dict[type, str] = {}):
        super().__init__("Main Toolbar")
        self.presenter = presenter
        
        group = QButtonGroup(self)
        group.setExclusive(True)

        is_dark = _is_dark_theme()
        icon_color = 'white' if is_dark else 'black'

        for tool in tool_set:
            button = QToolButton()
            self.addWidget(button)
            button.setText(type(tool).__name__)

            path = tool_icon_map.get(type(tool), None)
            if path:
                pixmap = QPixmap(path)
                mask = pixmap.mask()
                pixmap.fill(QColor(icon_color))
                pixmap.setMask(mask)
                icon = QIcon(pixmap)
            else:
                icon = QIcon()
            
            button.setIcon(icon)

            button.setToolTip(tool.name)
            button.clicked.connect(lambda checked, t=tool: self._set_current_tool(t))
            group.addButton(button)

    def _set_current_tool(self, tool):
        self.presenter.current_tool = tool

    def _on_current_tool_changed(self, tool):
        # Handle changing style of buttons based on current tool
        pass
    