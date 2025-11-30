from PySide6.QtGui import QTransform
from PySide6.QtWidgets import (
    QDialog, QVBoxLayout, QLabel, 
    QPushButton, QTableWidget, QTableWidgetItem, 
    QHeaderView, QAbstractItemView, QMessageBox
)
from PySide6.QtCore import Qt


from commands import ZoneConnectionAddCommand, ZoneConnectionRemoveCommand
from cad_scene import InteractiveScene

from .tool import Tool
from model import Zone, Floor

from view import HighlightPreview

def get_legal_zones_to_connect(zone: Zone):
    return [z for z in zone.floor.zones if z != zone]

def get_zones_connected_to(zone: Zone):
    building = zone.floor.building
    if building:
        return building.get_zones_connected_to(zone)
    return set()

class ZoneSelectionDialog(QDialog):
    def __init__(self, zone, parent=None):
        super().__init__(parent)

        self._selected_zone = None
        self._legal_zones = get_legal_zones_to_connect(zone)

        self._init_ui(zone)

    def get_selected_zone(self):
        return self._selected_zone

    def _init_ui(self, zone):
        self.setWindowTitle("Select zone to connect")
        self.resize(600, 400)
        layout = QVBoxLayout(self)

        # Title label
        title = f"Select zone to connect \"{zone.name}\" to:"
        label = QLabel(title)
        label.setAlignment(Qt.AlignCenter)
        label.setStyleSheet("font-weight: bold; font-size: 16px; margin: 10px;")
        layout.addWidget(label)

        # Table of possible connections
        self._table = QTableWidget(len(self._legal_zones), 2)
        self._table.setHorizontalHeaderLabels(["Zone name", "Floor"])
        self._table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self._table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self._table.setSelectionMode(QAbstractItemView.SingleSelection)
        self._table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self._table.horizontalHeader().setSectionsMovable(False)
        self._table.cellDoubleClicked.connect(self._accept_selection)
        layout.addWidget(self._table)
        self._refresh_table()

        # Connect button
        btn_connect = QPushButton("+ Connect")
        btn_connect.setStyleSheet("font-weight: bold; font-size: 14px; padding: 10px;")
        btn_connect.clicked.connect(self._accept_selection)
        layout.addWidget(btn_connect)

    def _refresh_table(self):
        self._table.clearContents()
        self._table.setRowCount(len(self._legal_zones))
        
        for i, zone in enumerate(self._legal_zones):
            floor_name = zone.floor.name
            
            floor_item = QTableWidgetItem(floor_name)
            zone_item = QTableWidgetItem(zone.name)
            
            floor_item.setTextAlignment(Qt.AlignCenter)
            zone_item.setTextAlignment(Qt.AlignCenter)
            
            self._table.setItem(i, 0, floor_item)
            self._table.setItem(i, 1, zone_item)

    def _accept_selection(self):
        selected_rows = self._table.selectionModel().selectedRows()
        
        if not selected_rows:
            QMessageBox.warning(self, "Warning", "Please select a zone first.")
            return

        row_idx = selected_rows[0].row()
        self._selected_zone = self._legal_zones[row_idx]
        
        self.accept()


class AddZoneConnectionDialog(QDialog):
    def __init__(self, controller, zone: Zone, parent=None):
        super().__init__(parent)
        self._controller = controller
        self._selected_zone = zone
        self._connected_zones = get_zones_connected_to(zone)

        self._table = None
        self._building = zone.floor.building

        self._init_ui(zone)

    def _init_ui(self, zone: Zone):
        self.setWindowTitle("Zone connections")
        self.resize(600, 400)

        layout = QVBoxLayout(self)

        title_label = self._get_title_widget(zone)
        layout.addWidget(title_label)

        self._table = self._get_table_widget()
        layout.addWidget(self._table)
        self._refresh_table()

        btn_add = QPushButton("+")
        btn_add.clicked.connect(self._add_connection)
        layout.addWidget(btn_add)

    def _get_table_widget(self):
        table = QTableWidget(0, 3)
        table.setHorizontalHeaderLabels(["Floor", "Zone name", "Action"])
        table.setSelectionBehavior(QAbstractItemView.SelectRows)
        table.setSelectionMode(QAbstractItemView.SingleSelection)
        table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        table.horizontalHeader().setSectionResizeMode(0, QHeaderView.Stretch)
        table.horizontalHeader().setSectionResizeMode(1, QHeaderView.Stretch)
        table.horizontalHeader().setSectionResizeMode(2, QHeaderView.Stretch)
        table.setColumnWidth(2, 80)
        return table
    
    def _get_title_widget(self, zone: Zone) -> QLabel:
        title = f"Zone \"{zone.name}\" at \"{zone.floor.name}\" connections:"
        title_label = QLabel(title)
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet("font-weight: bold; font-size: 16px; margin: 10px;")
        return title_label

    def _refresh_table(self):
        self._table.clearContents()
        self._table.setRowCount(0)
        self._connected_zones = get_zones_connected_to(self._selected_zone)
        for zone in self._connected_zones:
            self._add_row(zone.floor, zone)

    def _add_row(self, floor: Floor, zone: Zone):
        row_idx = self._table.rowCount()
        
        self._table.insertRow(row_idx)

        item = QTableWidgetItem(floor.name)
        item.setTextAlignment(Qt.AlignCenter)
        self._table.setItem(row_idx, 0, item)

        item = QTableWidgetItem(zone.name)
        item.setTextAlignment(Qt.AlignCenter)
        self._table.setItem(row_idx, 1, item)

        btn_remove = QPushButton("Delete")
        btn_remove.clicked.connect(lambda: self._remove_connection(self._selected_zone, zone))
        
        self._table.setCellWidget(row_idx, 2, btn_remove)

    def _add_connection(self):
        dialog = ZoneSelectionDialog(self._selected_zone, parent=self)
        result = dialog.exec()

        if result == QDialog.Accepted:
            target_zone = dialog.get_selected_zone()
    
            if target_zone:
                zone_a, zone_b = self._selected_zone, target_zone
                command = ZoneConnectionAddCommand(self._building, zone_a, zone_b)
                self._controller.execute(command)
                self._refresh_table()

    def _remove_connection(self, zone_a: Zone, zone_b: Zone):
        self._building.remove_connection(zone_a, zone_b)
        self._refresh_table()


class ZoneConnectTool(Tool):
    def __init__(self, presenter, scene: InteractiveScene, name="Zone connections"):
        super().__init__(presenter, scene, name)
        self._highlight_preview = HighlightPreview()

    def mouse_click(self, pos, modifier=None):
        item = self._scene.itemAt(pos, QTransform())
        el = self._presenter.get_model_for_item(item)

        if type(el) == Zone:
            parent = self._scene.views()[0] if self._scene.views() else None
            window = AddZoneConnectionDialog(self._presenter, el, parent)
            window.show()
    
    def mouse_move(self, pos):
        item = self._scene.itemAt(pos, QTransform())

        if type(self._presenter.get_model_for_item(item)) == Zone:
            self._highlight_preview.update_preview(item)
        else:
            self._highlight_preview.clear()