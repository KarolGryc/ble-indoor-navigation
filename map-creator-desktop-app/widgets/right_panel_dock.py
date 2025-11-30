from PySide6.QtWidgets import QDockWidget, QWidget, QVBoxLayout
from PySide6.QtCore import Qt

class RightPanelDock(QDockWidget):
    def __init__(self, parent, layers_panel, floor_list_widget):
        super().__init__("View settings", parent)
        self.setFeatures(QDockWidget.DockWidgetMovable)
        self.setAllowedAreas(Qt.LeftDockWidgetArea | Qt.RightDockWidgetArea)
        
        container = QWidget()
        layout = QVBoxLayout(container)
        
        layout.addWidget(layers_panel)
        layout.addWidget(floor_list_widget)
        
        self.setWidget(container)