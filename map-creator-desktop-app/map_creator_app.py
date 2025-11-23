from PySide6.QtWidgets import QMainWindow, QGraphicsScene, QPushButton, QWidget, QVBoxLayout, QHBoxLayout
from PySide6.QtGui import QColor

from model.map_model import MapModel
from map_view import MapView
from map_presenter import MapPresenter
from tools.node_add_tool import NodeAddTool
from tools.wall_add_tool import WallAddTool

from toolbar import Toolbar

class MapCreatorApp(QMainWindow):
    def __init__(self, screen_size=(1280, 720)):
        super().__init__()
        self.setWindowTitle("Map Creator App")
        self.setGeometry(0, 0, *screen_size)

        # MODEL + PRESENTER + SCENE
        model = MapModel()
        scene = QGraphicsScene()
        scene.setSceneRect(-50000, -50000, 100000, 100000)

        self.presenter = MapPresenter(model, scene)

        tools = [NodeAddTool(self.presenter, scene),
                 WallAddTool(self.presenter, scene)]
        toolbar = Toolbar(self.presenter, tools)
        self.addToolBar(toolbar)

        # --- Widok mapy
        self.view = MapView(self.presenter)
        self.setCentralWidget(self.view)