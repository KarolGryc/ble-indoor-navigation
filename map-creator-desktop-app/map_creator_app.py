from PySide6.QtWidgets import QMainWindow, QVBoxLayout, QWidget, QDockWidget, QMessageBox
from PySide6.QtGui import QShortcut, QKeySequence
from PySide6.QtCore import Qt

from model.floor import Floor
from view.map_view import MapView
from main_map_controller import MainMapController
from cad_scene import InteractiveScene

from tools.wall_add_tool import WallAddTool
from tools.select_tool import SelectTool
from tools.zone_add_tool import ZoneAddTool
from tools.renaming_tool import RenamingTool
from tools.point_of_interest_add_tool import PointOfInterestAddTool

from widgets.toolbar import Toolbar

from widgets.app_menu import AppMenu

from model.building import Building

from widgets.floor_list import AutoSyncFloorList
from widgets.layers_panel import LayersPanel

from commands.floor_add_command import FloorAddCommand
from commands.floor_remove_command import FloorRemoveCommand

from utils.general import ask_floor_name

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
        presenter = MainMapController(building_model, scene)
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
            WallAddTool: "icons/tools-icons/wall_add.svg",
            SelectTool: "icons/tools-icons/select_move.svg",
            ZoneAddTool: "icons/tools-icons/zone_add.svg",
            RenamingTool: "icons/tools-icons/edit_item.svg",
            PointOfInterestAddTool: "icons/tools-icons/location_add.svg",
        }

        toolbar = Toolbar(presenter, tools, tool_icon_map)

        self.addToolBar(Qt.LeftToolBarArea, toolbar)

        ###############################
        # MAIN VIEW
        ################################
        self.view = MapView(presenter)
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
        floor_list.floor_selected.connect(lambda floor: setattr(presenter, 'current_floor', floor))
        floor_list.add_floor_request.connect(lambda: self._add_floor(presenter, building_model))
        floor_list.remove_floor_request.connect(lambda floor: self._remove_floor(presenter, building_model, floor))
        floor_list.rename_floor_request.connect(lambda floor: self._rename_floor(floor))
        layout.addWidget(floor_list)

        dock = QDockWidget("View settings", self)
        dock.setFeatures(QDockWidget.DockWidgetMovable)
        dock.setAllowedAreas(Qt.LeftDockWidgetArea | Qt.RightDockWidgetArea)
        dock.setWidget(right_panel)

        self.addDockWidget(Qt.RightDockWidgetArea, dock)

        ###############################
        # MENU BAR
        ###############################
        self._create_menu_bar(presenter)

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

    def _create_model(self) -> Building:
        model = Building()
        model.add_floor()
        return model
    
    def _create_menu_bar(self, presenter: MainMapController):
        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)
        self.menu_bar.undo_triggered.connect(presenter.undo)
        self.menu_bar.redo_triggered.connect(presenter.redo)