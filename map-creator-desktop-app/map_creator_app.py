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
    Toolbar, AppMenu, FloorView, AutoSyncFloorList, LayersPanel
)

from commands import FloorAddCommand, FloorRemoveCommand

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

        type_to_graphics_item = {
            Node: NodeGraphicsItem,
            Wall: WallGraphicsItem,
            Zone: ZoneGraphicsItem,
            PointOfInterest: PointOfInterestGraphicsItem,
        }
        grid_size = 25
    
        presenter = MainMapController(building_model, scene, grid_size, type_to_graphics_item)
        scene.set_presenter(presenter)

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
            ZoneConnectTool(presenter, scene)
        ]

        icons_path = "icons/tools-icons/"
        icon_format = ".png"
        tool_icon_map = {
            WallAddTool: f"{icons_path}wall_add{icon_format}",
            SelectTool: f"{icons_path}select_move{icon_format}",
            ZoneAddTool: f"{icons_path}zone_add{icon_format}",
            RenamingTool: f"{icons_path}item_edit{icon_format}",
            PointOfInterestAddTool: f"{icons_path}location_add{icon_format}",
            ZoneConnectTool: f"{icons_path}zone_connection{icon_format}"
        }

        toolbar = Toolbar(presenter, tools, tool_icon_map)
        self.addToolBar(Qt.LeftToolBarArea, toolbar)

        ###############################
        # MAIN VIEW
        ################################
        view = FloorView(presenter)
        self.setCentralWidget(view)

        ###############################
        # RIGHT PANEL
        ###############################
        right_panel = QWidget()
        layout = QVBoxLayout(right_panel)

        layers_panel = LayersPanel(scene)
        layers_panel.active_class_changed.connect(lambda t: setattr(scene, "active_item_type", t))
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
        self._create_menu_bar(presenter, view)

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
    
    def _create_menu_bar(self, presenter: MainMapController, view: FloorView):
        self.menu_bar = AppMenu(self)
        self.setMenuBar(self.menu_bar)
        self.menu_bar.undo_triggered.connect(presenter.undo)
        self.menu_bar.redo_triggered.connect(presenter.redo)
        self.menu_bar.map_theme_triggered.connect(lambda theme: setattr(view, 'map_theme', theme))