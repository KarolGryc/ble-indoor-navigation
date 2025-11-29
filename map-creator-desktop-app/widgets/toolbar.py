from PySide6.QtWidgets import QToolBar, QToolButton, QButtonGroup, QLabel, QWidget, QSizePolicy
from PySide6.QtGui import QIcon, QPixmap, QColor
from PySide6.QtCore import QSize
from main_map_controller import MainMapController

from tools.tool import Tool

from utils.general import _is_dark_theme

class Toolbar(QToolBar):
    def __init__(self, 
                 presenter: MainMapController, 
                 tool_set: list[Tool], 
                 tool_icon_map: dict[type, str] = {}):
        super().__init__("Main Toolbar")
        self._presenter = presenter

        icon_size = 28
        self.setIconSize(QSize(icon_size, icon_size))

        group = QButtonGroup(self)
        group.setExclusive(True)

        is_dark = _is_dark_theme()
        icon_color = 'white' if is_dark else 'black'

        for tool in tool_set:
            button = QToolButton()
            button.setCheckable(True)
            self.addWidget(button)
            button.setText(tool.name)

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

        self.addSeparator()
        spacer = QWidget()
        spacer.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        self.addWidget(spacer)
        self.addSeparator()

        self._x_label = QLabel("X:")
        self._x_val_label = QLabel("0.0")
        self._y_label = QLabel("Y:")
        self._y_val_label = QLabel("0.0")

        self.addWidget(self._x_label)
        self.addWidget(self._x_val_label)
        self.addWidget(self._y_label)
        self.addWidget(self._y_val_label)

        presenter.pointer_canvas_moved.connect(self._on_pointer_canvas_moved)
    
    def _on_pointer_canvas_moved(self, pos):
        pos = pos / 100
        self._x_val_label.setText(f"{pos.x():>6.1f}m")
        self._y_val_label.setText(f"{pos.y():>6.1f}m")

    def _set_current_tool(self, tool):
        self._presenter.current_tool = tool
    