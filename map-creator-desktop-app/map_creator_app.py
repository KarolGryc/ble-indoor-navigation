from PySide6.QtWidgets import QMainWindow
from PySide6.QtGui import QShortcut, QKeySequence
from PySide6.QtCore import Qt

from model.map_model import MapModel
from tools.select_tool import SelectTool
from view.map_view import MapView
from map_presenter import MapPresenter
from tools.wall_add_tool import WallAddTool
from cad_scene import InteractiveScene

from toolbar import Toolbar

class MapCreatorApp(QMainWindow):
    def __init__(self, screen_size=(1280, 720)):
        super().__init__()
        self.setWindowTitle("Map Creator App")
        self.setGeometry(0, 0, *screen_size)

        model = MapModel()
        scene = InteractiveScene()
        scene.setSceneRect(-5000, -5000, 10000, 10000)

        self.presenter = MapPresenter(model, scene)
        scene.set_presenter(self.presenter)

        # Tool deactivation shortcut
        self.delete_shorcut = QShortcut(QKeySequence(Qt.Key_Escape), self)
        self.delete_shorcut.activated.connect(self.presenter.reset_current_tool)

        tools = [WallAddTool(self.presenter, scene),
                 SelectTool(self.presenter, scene)]
        toolbar = Toolbar(self.presenter, tools)
        self.addToolBar(toolbar)

        self.view = MapView(self.presenter)
        self.setCentralWidget(self.view)