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
        self.presenter = MapPresenter(building_model, scene)
        scene.set_presenter(self.presenter)

        # Tool deactivation shortcut
        self.delete_shorcut = QShortcut(QKeySequence(Qt.Key_Escape), self)
        self.delete_shorcut.activated.connect(self.presenter.reset_current_tool)

        ###############################
        # TOOLS TOOLBAR
        ###############################
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

        right_toolbar = QToolBar("Right Toolbar", self)
        right_toolbar.setOrientation(Qt.Vertical)
        right_toolbar.setMovable(False) 
        self.addToolBar(Qt.RightToolBarArea, right_toolbar)



        ###############################
        # LAYER SELECTOR TOOLBAR
        ###############################
        layer_group = QActionGroup(self)
        layer_group.setExclusive(True)

        def add_layer_action(name, item_type):
            action = right_toolbar.addAction(name)
            action.setCheckable(True)
            layer_group.addAction(action)
            action.triggered.connect(lambda ch: ch and scene.set_active_item_type(item_type))
            
            return action

        act_wall = add_layer_action("Walls", Wall)
        act_zone = add_layer_action("Zones", Zone)
        act_poi  = add_layer_action("POIs", PointOfInterest)

        act_wall.setChecked(True)
        scene.set_active_item_type(Wall)


        ###############################
        # MAIN VIEW
        ################################
        self.view = MapView(self.presenter)
        # self.view.setViewport(QOpenGLWidget())
        self.setCentralWidget(self.view)


        ###############################
        # MENU BAR
        ###############################
        self._create_menu_bar()


        ###############################
        # FLOOR LIST
        ###############################
        right_panel = QWidget()
        layout = QVBoxLayout(right_panel)

        floor_list = AutoSyncFloorList(building_model)
        layout.addWidget(floor_list)

        floor_list.floor_selected.connect(lambda floor: print(f"Selected floor: {floor.name}"))
        floor_list.add_floor_requested.connect(lambda: print("Add floor requested"))
        floor_list.remove_floor_requested.connect(lambda index: print(f"Remove floor at index requested: {index}"))

        dock = QDockWidget("Floors", self)

        dock.setFeatures(QDockWidget.DockWidgetMovable)

        dock.setAllowedAreas(Qt.LeftDockWidgetArea | Qt.RightDockWidgetArea)

        dock.setWidget(right_panel)

        self.addDockWidget(Qt.RightDockWidgetArea, dock)

    def _create_model(self) -> Building:
        model = Building()
        model.add_floor()
        return model
    
    def _create_menu_bar(self):
        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)
        self.menu_bar.undo_triggered.connect(self.presenter.undo)
        self.menu_bar.redo_triggered.connect(self.presenter.redo)