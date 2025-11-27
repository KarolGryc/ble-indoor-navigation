from PySide6.QtWidgets import QMainWindow, QToolBar, QVBoxLayout, QWidget, QHBoxLayout, QPushButton, QDockWidget
from PySide6.QtGui import QShortcut, QKeySequence, QActionGroup
from PySide6.QtCore import Qt

from model.floor import Floor
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

from model.wall import Wall
from model.zone import Zone
from model.point_of_interest import PointOfInterest
from model.building import Building

from view.floor_list import AutoSyncFloorList
from view.layers_panel import LayersPanel

from commands.floor_add_command import FloorAddCommand
from commands.floor_remove_command import FloorRemoveCommand

class MapCreatorApp(QMainWindow):
    def __init__(self,
                 parent=None,
                 screen_size=(1280, 720), 
                 window_title="Map Creator App"):
        super().__init__(parent)
        self.setWindowTitle(window_title)
        self.setGeometry(0, 0, *screen_size)
        
        scene = InteractiveScene()
        scene.setSceneRect(-5000, -5000, 10000, 10000)

        building_model = self._create_model()
        presenter = MapPresenter(building_model, scene)
        scene.set_presenter(presenter)

        # Tool deactivation shortcut
        delete_shortcut = QShortcut(QKeySequence(Qt.Key_Escape), self)
        delete_shortcut.activated.connect(presenter.reset_current_tool)

        ###############################
        # TOOLS TOOLBAR
        ###############################
        tools = [
            WallAddTool(presenter, scene),
            SelectTool(presenter, scene),
            ZoneAddTool(presenter, scene),
            RenamingTool(presenter, scene),
            PointOfInterestAddTool(presenter, scene),
        ]

        tool_icon_map = {
            WallAddTool: "icons/generic.png",
            SelectTool: "icons/generic.png",
            ZoneAddTool: "icons/generic.png",
            RenamingTool: "icons/generic.png",
            PointOfInterestAddTool: "icons/generic.png",
        }

        toolbar = Toolbar(presenter, tools, tool_icon_map)

        self.addToolBar(Qt.LeftToolBarArea, toolbar)

        ###############################
        # MAIN VIEW
        ################################
        self.view = MapView(presenter)
        # self.view.setViewport(QOpenGLWidget())
        self.setCentralWidget(self.view)

        ###############################
        # RIGHT PANEL
        ###############################
        right_panel = QWidget()
        layout = QVBoxLayout(right_panel)

        layers_panel = LayersPanel(scene)
        layers_panel.active_class_changed.connect(scene.set_active_item_type)
        layout.addWidget(layers_panel)

        floor_list = AutoSyncFloorList(building_model)
        floor_list.floor_selected.connect(lambda floor: print(f"Selected floor: {floor.name}"))
        floor_list.add_floor_requested.connect(lambda: presenter.execute(FloorAddCommand(building_model)))
        floor_list.remove_floor_requested.connect(lambda floor: presenter.execute(FloorRemoveCommand(building_model, floor)))
        layout.addWidget(floor_list)

        dock = QDockWidget("Floors", self)
        dock.setFeatures(QDockWidget.DockWidgetMovable)
        dock.setAllowedAreas(Qt.LeftDockWidgetArea | Qt.RightDockWidgetArea)
        dock.setWidget(right_panel)

        self.addDockWidget(Qt.RightDockWidgetArea, dock)

        ###############################
        # MENU BAR
        ###############################
        self._create_menu_bar(presenter)

    def _create_model(self) -> Building:
        model = Building()
        model.add_floor()
        return model
    
    def _create_menu_bar(self, presenter: MapPresenter):
        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)
        self.menu_bar.undo_triggered.connect(presenter.undo)
        self.menu_bar.redo_triggered.connect(presenter.redo)