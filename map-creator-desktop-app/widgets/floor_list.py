from PySide6.QtWidgets import (
    QWidget, QVBoxLayout, QHBoxLayout, QLabel,
    QListWidget, QAbstractItemView, QListWidgetItem, QPushButton
)
from PySide6.QtCore import Qt, Signal

from model.building import Building
from model.floor import Floor

class AutoSyncFloorList(QWidget):
    floor_selected = Signal(Floor)
    add_floor_request = Signal()
    remove_floor_request = Signal(Floor)
    rename_floor_request = Signal(Floor)

    def __init__(self, building: Building, parent=None):
        super().__init__(parent)

        # assign building and connect signals
        self._building = building
        self._building.floor_added.connect(self.refresh_view)
        self._building.floor_removed.connect(self.refresh_view)
        self._building.floor_name_changed.connect(self.refresh_view)

        layout = QVBoxLayout(self)

        title_label = QLabel("Choose active floor:")
        layout.addWidget(title_label) 

        # Create list widget and add to layout
        self._list_widget = QListWidget()
        self._list_widget.currentItemChanged.connect(self._on_selection_changed)
        layout.addWidget(self._list_widget)

        # Create Add and remove buttons
        _add_button = QPushButton("Add")
        _remove_button = QPushButton("Remove")
        _rename_button = QPushButton("Rename")
        _add_button.clicked.connect(self.add_floor_request)
        _remove_button.clicked.connect(lambda: self.remove_floor_request.emit(self.current_floor))
        _rename_button.clicked.connect(lambda: self.rename_floor_request.emit(self.current_floor))

        button_layout = QHBoxLayout()
        button_layout.addWidget(_add_button)
        button_layout.addWidget(_remove_button)
        button_layout.addWidget(_rename_button)

        layout.addLayout(button_layout)

        self._list_widget.setDragDropMode(QAbstractItemView.InternalMove)
        self._list_widget.model().rowsMoved.connect(self._on_user_reordered)
        self.refresh_view()

    def refresh_view(self):
        self._list_widget.clear()
        for floor in self._building.floors:
            item = QListWidgetItem(str(floor.name))
            item.setData(Qt.UserRole, floor)
            self._list_widget.addItem(item)

    def _on_user_reordered(self):
        new_order = []
        for i in range(self._list_widget.count()):
            item = self._list_widget.item(i)
            new_order.append(item.data(Qt.UserRole))
        
        self._building.floors = new_order
        self.refresh_view()

    def _on_selection_changed(self, current: QListWidgetItem, previous: QListWidgetItem):
        if current is not None:
            clicked_floor = current.data(Qt.UserRole)

            self.floor_selected.emit(clicked_floor)

    @property
    def current_floor(self) -> Floor | None:
        current_item = self._list_widget.currentItem()
        if current_item is not None:
            return current_item.data(Qt.UserRole)
        return None