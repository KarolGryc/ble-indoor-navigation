from PySide6.QtWidgets import QMainWindow
from PySide6.QtGui import QShortcut, QKeySequence
from PySide6.QtCore import Qt

from model.map_model import MapModel
from view.map_view import MapView
from map_presenter import MapPresenter
from cad_scene import InteractiveScene

from tools.wall_add_tool import WallAddTool
from tools.select_tool import SelectTool
from tools.zone_add_tool import ZoneAddTool
from tools.renaming_tool import RenamingTool
from tools.point_of_interest_add_tool import PointOfInterestAddTool

from toolbar import Toolbar

from PySide6.QtOpenGLWidgets import QOpenGLWidget

from app_menu import AppMenu

class MapCreatorApp(QMainWindow):
    def __init__(self,
                 parent=None,
                 screen_size=(1280, 720), 
                 window_title="Map Creator App"):
        super().__init__(parent)
        self.setWindowTitle(window_title)
        self.setGeometry(0, 0, *screen_size)

        model = MapModel()
        scene = InteractiveScene()
        scene.setSceneRect(-5000, -5000, 10000, 10000)

        self.presenter = MapPresenter(model, scene)
        scene.set_presenter(self.presenter)

        # Tool deactivation shortcut
        self.delete_shorcut = QShortcut(QKeySequence(Qt.Key_Escape), self)
        self.delete_shorcut.activated.connect(self.presenter.reset_current_tool)

        tools = [
            WallAddTool(self.presenter, scene),
            SelectTool(self.presenter, scene),
            ZoneAddTool(self.presenter, scene),
            RenamingTool(self.presenter, scene),
            PointOfInterestAddTool(self.presenter, scene),
        ]
        tool_icon_map = {
            WallAddTool: "icons/generic.png",
            SelectTool: "icons/generic.png",
            ZoneAddTool: "icons/generic.png",
            RenamingTool: "icons/generic.png",
            PointOfInterestAddTool: "icons/generic.png",
        }

        toolbar = Toolbar(self.presenter, tools, tool_icon_map)

        self.addToolBar(Qt.LeftToolBarArea, toolbar)

        self.view = MapView(self.presenter)
        # self.view.setViewport(QOpenGLWidget())
        self.setCentralWidget(self.view)

        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)
        self.menu_bar.undo_triggered.connect(self.presenter.undo)
        self.menu_bar.redo_triggered.connect(self.presenter.redo)