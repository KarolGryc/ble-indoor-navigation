import json
from PySide6.QtWidgets import QMainWindow, QVBoxLayout, QWidget, QDockWidget, QMessageBox
from PySide6.QtGui import QShortcut, QKeySequence
from PySide6.QtCore import Qt

from main_map_controller import MainMapController
from cad_scene import InteractiveScene

from tools import (
    WallAddTool, SelectTool, ZoneAddTool, 
    RenamingTool, PointOfInterestAddTool, ZoneConnectTool
)

from model import (
    Building, Floor, Node, Wall, Zone, PointOfInterest
)

from view import (
    NodeGraphicsItem, WallGraphicsItem, 
    ZoneGraphicsItem, PointOfInterestGraphicsItem
)

from widgets import (
    Toolbar, AppMenu, FloorView, AutoSyncFloorList, LayersPanel, RightPanelDock
)

from commands import FloorAddCommand, FloorRemoveCommand

from utils.general import ask_floor_name, load_building, save_building

from constants import icons, GRID_SIZE_DEFAULT

class MapCreatorApp(QMainWindow):
    def __init__(self,
                 parent=None,
                 screen_size=(1280, 720), 
                 window_title="Map Creator App"):
        super().__init__(parent)
        self._setup_window(screen_size, window_title)

        self._building_model: Building = None
        self._scene: InteractiveScene = None
        self._controller: MainMapController = None
        self._floor_view: FloorView = None

        self._setup_core()
        self._setup_ui()
        self._setup_shortcuts()

    def _setup_window(self, screen_size, window_title):
        self.setWindowTitle(window_title)
        self.resize(*screen_size)

    def _setup_core(self):
        type_to_graphics_item = {
            Node: NodeGraphicsItem,
            Wall: WallGraphicsItem,
            Zone: ZoneGraphicsItem,
            PointOfInterest: PointOfInterestGraphicsItem,
        }

        self._building_model = Building()
        self._building_model.add_floor(Floor("Ground Floor"))
        self._scene = InteractiveScene()
        x, y, h, w = -5000, -5000, 10000, 10000
        self._scene.setSceneRect(x, y, h, w)
        self._controller = MainMapController(self._building_model, 
                                             self._scene, 
                                             GRID_SIZE_DEFAULT,
                                             type_to_graphics_item)
        
        self._scene.set_controller(self._controller)

    def _setup_ui(self):
        # Add main floor view
        self._floor_view = FloorView(self._controller)
        self.setCentralWidget(self._floor_view)

        # Add toolbar
        self._setup_toolbar()

        # Add right dock
        layers_panel = LayersPanel(self._scene)
        layers_panel.active_class_changed.connect(lambda t: setattr(self._scene, "active_item_type", t))

        floor_list = AutoSyncFloorList(self._controller)
        floor_list.floor_selected.connect(
            lambda floor: setattr(self._controller, 'current_floor', floor)
        )
        floor_list.add_floor_request.connect(
            lambda: self._add_floor(self._controller, self._building_model)
        )
        floor_list.remove_floor_request.connect(
            lambda floor: self._remove_floor(self._controller, self._building_model, floor)
        )
        floor_list.rename_floor_request.connect(
            lambda floor: self._rename_floor(floor)
        )

        right_panel = RightPanelDock(self, layers_panel, floor_list)
        self.addDockWidget(Qt.RightDockWidgetArea, right_panel)

        self._create_menu_bar(self._controller, self._floor_view)

    def _setup_shortcuts(self):
        delete_shortcut = QShortcut(QKeySequence(Qt.Key_Escape), self)
        delete_shortcut.activated.connect(self._controller.reset_current_tool)

    def _setup_toolbar(self):
        tools = [
            WallAddTool,
            SelectTool,
            ZoneAddTool,
            RenamingTool,
            PointOfInterestAddTool,
            ZoneConnectTool
        ]

        tool_icon_map = {
            WallAddTool: icons["add_wall"],
            SelectTool: icons["select_move"],
            ZoneAddTool: icons["add_zone"],
            RenamingTool: icons["edit_item"],
            PointOfInterestAddTool: icons["add_poi"],
            ZoneConnectTool: icons["connect_zones"]
        }

        toolbar = Toolbar(self._controller, tools, tool_icon_map)
        self.addToolBar(Qt.LeftToolBarArea, toolbar)


    def _add_floor(self, presenter: MainMapController, building_model: Building):
        name = ask_floor_name("Add Floor", "New Floor")
        if name:
            new_floor = Floor(name)
            cmd = FloorAddCommand(building_model, new_floor)
            presenter.execute(cmd)

        else:
            QMessageBox.critical(
                self,
                "Error",
                "Can't add floor without a name."
            )

    def _remove_floor(self, presenter: MainMapController, building_model: Building, floor: Floor):
        if len(building_model.floors) <= 1:
            QMessageBox.critical(
                self,
                "Error",
                "Building must have at least one floor."
            )
            return

        cmd = FloorRemoveCommand(building_model, floor)
        presenter.execute(cmd)

    def _rename_floor(self, floor: Floor):
        new_name = ask_floor_name("Rename Floor", floor.name)
        if new_name:
            floor.name = new_name
    
    def _reset_state(self):
        building = Building()
        building.add_floor(Floor("Ground Floor"))
        self._controller.building = building

    def _create_menu_bar(self, presenter: MainMapController, view: FloorView):
        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)

        # File signals
        # self.menu_bar.new_file_requested.connect(self._reset_state)
        self.menu_bar.load_requested.connect(lambda: setattr(self._controller, 'building', load_building(self)))
        self.menu_bar.save_requested.connect(lambda: save_building(self, self._building_model))
        
        # Edit signals
        self.menu_bar.redo_triggered.connect(presenter.redo)
        self.menu_bar.undo_triggered.connect(presenter.undo)

        # View signals
        self.menu_bar.map_theme_triggered.connect(lambda theme: setattr(view, 'map_theme', theme))