from PySide6.QtWidgets import QMainWindow, QGraphicsScene
from PySide6.QtGui import QColor

from map_model import MapModel
from map_view import MapView
from map_presenter import MapPresenter

class MapCreatorApp(QMainWindow):
    def __init__(self, screen_size=(1280, 720)):
        super().__init__()
        self.setWindowTitle("Map Creator App")
        self.setGeometry(0, 0, *screen_size)
        
        # add Map model, view and presenter
        model = MapModel()

        scene = QGraphicsScene()
    
        presenter = MapPresenter(model, scene)

        view = MapView(presenter)
        self.setCentralWidget(view)