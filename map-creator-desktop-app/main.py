import sys
from PySide6.QtWidgets import QApplication, QMainWindow, QHBoxLayout, QWidget
from PySide6.QtGui import QShortcut, QKeySequence
from view import MapView
from viewmodel import BuildingViewModel
from tool_bar import ToolBar
from PySide6.QtCore import Qt
from tools import *

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()

        self.viewmodel = BuildingViewModel()

        self._set_shortcuts()

        layout = QHBoxLayout()

        toolbar = ToolBar(self.viewmodel)
        self.addToolBar(Qt.LeftToolBarArea, toolbar)

        self.map_view = MapView(self.viewmodel)

        layout.addWidget(self.map_view)

        container = QWidget()
        container.setLayout(layout)
        self.setCentralWidget(container)

        self.setWindowTitle("Map Maker")
        self.resize(800, 600)



    def _set_shortcuts(self):
        QShortcut(QKeySequence("W"), self, activated=lambda: setattr(self.viewmodel, "active_tool", WallTool()))
        QShortcut(QKeySequence("S"), self, activated=lambda: setattr(self.viewmodel, "active_tool", SelectTool()))


app = QApplication(sys.argv)
window = MainWindow()
window.show()
app.exec()