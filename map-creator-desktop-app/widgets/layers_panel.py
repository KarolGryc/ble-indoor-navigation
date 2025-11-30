from PySide6.QtWidgets import QWidget, QVBoxLayout, QPushButton, QButtonGroup, QSizePolicy, QLabel
from PySide6.QtCore import Signal

from model.wall import Wall
from model.zone import Zone
from model.point_of_interest import PointOfInterest

class LayersPanel(QWidget):
    active_class_changed = Signal(type)

    def __init__(self, scene, parent=None):
        super().__init__(parent)
        self._scene = scene
        
        layout = QVBoxLayout(self)
        layout.setSpacing(5)
        layout.setContentsMargins(5, 5, 5, 5)
        
        title_label = QLabel("Choose active layer type:")
        layout.addWidget(title_label) 

        layout.addStretch() 

        self.btn_group = QButtonGroup(self)
        self.btn_group.setExclusive(True)

        self._add_layer_btn("Walls", Wall, layout)
        self._add_layer_btn("Zones", Zone, layout)
        self._add_layer_btn("POIs", PointOfInterest, layout)

        first_btn = self.btn_group.button(0)
        if first_btn:
            first_btn.setChecked(True)
            self._scene.active_item_type = Wall
            self.active_class_changed.emit(Wall)

    def _add_layer_btn(self, text, item_type, layout):
        btn = QPushButton(text)
        btn.setCheckable(True)
        
        btn.setMinimumHeight(40) 
        btn.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Fixed)

        btn.clicked.connect(lambda: self.active_class_changed.emit(item_type))

        layout.insertWidget(layout.count() - 1, btn)

        self.btn_group.addButton(btn, self.btn_group.buttons().__len__())
