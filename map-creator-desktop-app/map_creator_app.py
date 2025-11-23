from PySide6.QtWidgets import QMainWindow, QGraphicsScene, QPushButton, QWidget, QVBoxLayout, QHBoxLayout
from PySide6.QtGui import QColor

from map_model import MapModel
from map_view import MapView
from map_presenter import MapPresenter
from node_add_tool import NodeAddTool
from wall_add_tool import WallAddTool

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
        # self.presenter.set_current_tool(NodeAddTool(self.presenter))
        self.presenter.set_current_tool(WallAddTool(self.presenter, scene))

        central = QWidget()
        layout = QVBoxLayout(central)
        self.setCentralWidget(central)

        button_bar = QHBoxLayout()
        self.undo_button = QPushButton("Undo")
        self.undo_button.clicked.connect(self.presenter.undo_stack.undo)

        self.redo_button = QPushButton("Redo")
        self.redo_button.clicked.connect(self.presenter.undo_stack.redo)

        button_bar.addWidget(self.undo_button)
        button_bar.addWidget(self.redo_button)

        layout.addLayout(button_bar)

        # --- Widok mapy
        self.view = MapView(self.presenter)
        layout.addWidget(self.view)
