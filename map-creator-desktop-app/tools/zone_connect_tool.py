from PySide6.QtGui import QTransform
from PySide6.QtWidgets import (QDialog, QVBoxLayout, QLabel, 
                               QPushButton, QTableWidget, QTableWidgetItem, 
                               QHeaderView, QAbstractItemView, QMessageBox)
from PySide6.QtCore import Qt


from commands.zone_connection_add_command import ZoneConnectionAddCommand
from cad_scene import InteractiveScene
from tools.tool import Tool
from model.zone import Zone

def get_legal_zones_to_connect(zone: Zone):
    return [z for z in zone.floor.zones if z != zone]

class ZoneSelectionDialog(QDialog):
    def __init__(self, zone, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Select zone to connect")
        self.resize(600, 400)

        self._legal_zones = get_legal_zones_to_connect(zone)
        self._selected_zone = None

        layout = QVBoxLayout(self)

        # Title label
        label = QLabel(f"Connecting zone \"{zone.name}\" at \"{zone.floor.name}\" to:")
        label.setAlignment(Qt.AlignCenter)
        label.setStyleSheet("font-weight: bold; font-size: 16px; margin: 10px;")
        layout.addWidget(label)

        # Table of possible connections
        self._table = QTableWidget(len(self._legal_zones), 2)
        self._table.setHorizontalHeaderLabels(["Floor", "Zone name"])
        self._table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self._table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self._table.setSelectionMode(QAbstractItemView.SingleSelection)
        self._table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self._table.horizontalHeader().setSectionsMovable(False)
        self._table.cellDoubleClicked.connect(self.accept_selection)

        # Table contents
        for i, z in enumerate(self._legal_zones):
            floor_name = z.floor.name
            
            floor_item = QTableWidgetItem(floor_name)
            zone_item = QTableWidgetItem(z.name)
            
            floor_item.setTextAlignment(Qt.AlignCenter)
            zone_item.setTextAlignment(Qt.AlignCenter)
            
            self._table.setItem(i, 0, floor_item)
            self._table.setItem(i, 1, zone_item)

        layout.addWidget(self._table)

        # Connect button
        btn_connect = QPushButton("+ Connect")
        btn_connect.setStyleSheet("font-weight: bold; font-size: 14px; padding: 10px;")
        btn_connect.clicked.connect(self.accept_selection)
        layout.addWidget(btn_connect)

    def accept_selection(self):
        selected_rows = self._table.selectionModel().selectedRows()
        
        if not selected_rows:
            QMessageBox.warning(self, "Warning", "Please select a zone first.")
            return

        row_idx = selected_rows[0].row()
        self._selected_zone = self._legal_zones[row_idx]
        
        self.accept()

    def get_selected_zone(self):
        return self._selected_zone


class AddZoneConnectionDialog(QDialog):
    def __init__(self, controller, zone: Zone, parent=None):
        super().__init__(parent)
        self._controller = controller
        self._zone = zone
        self.setWindowTitle("Zone connections")
        self.resize(600, 400)

        layout = QVBoxLayout(self)

        title_label = QLabel(f"Zone \"{zone.name}\" at \"{zone.floor.name}\" connections:")
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet("font-weight: bold; font-size: 16px; margin: 10px;")
        layout.addWidget(title_label)

        btn_add = QPushButton("+")
        btn_add.clicked.connect(self.add_connection)
        layout.addWidget(btn_add)

        self.table = QTableWidget(0, 2)
        self.table.setHorizontalHeaderLabels(["Floor", "Zone name"])
        self.table.horizontalHeader().setSectionResizeMode(0, QHeaderView.Stretch)
        self.table.horizontalHeader().setSectionResizeMode(1, QHeaderView.Fixed)
        self.table.setColumnWidth(1, 50)
        self.table.verticalHeader().setVisible(False)
        
        layout.addWidget(self.table)

        letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G']
        for letter in letters:
            self.add_row(letter)

    def add_row(self, text):
        row_idx = self.table.rowCount()
        self.table.insertRow(row_idx)

        item = QTableWidgetItem(text)
        item.setTextAlignment(Qt.AlignCenter)
        self.table.setItem(row_idx, 0, item)

        btn_remove = QPushButton("-")
        btn_remove.clicked.connect(lambda: self.remove_row_by_button())
        
        self.table.setCellWidget(row_idx, 1, btn_remove)

    def remove_row_by_button(self):
        button = self.sender()
        if button:
            index = self.table.indexAt(button.pos())
            if index.isValid():
                self.table.removeRow(index.row())

    def add_connection(self):
        dialog = ZoneSelectionDialog(self._zone, parent=self)
        
        result = dialog.exec()

        if result == QDialog.Accepted:
            target_zone = dialog.get_selected_zone()
    
            if target_zone:
                command = ZoneConnectionAddCommand(self._controller.model, self._zone, target_zone)
                self._controller.execute(command)



class ZoneConnectTool(Tool):
    def __init__(self, presenter, scene: InteractiveScene, name="Zone connections"):
        super().__init__(presenter, scene, name)

        self._highlighted_item = None

    def mouse_click(self, pos, modifier=None):
        item = self._scene.itemAt(pos, QTransform())
        
        element = self._presenter.get_model_for_item(item)

        if type(element) == Zone:
            window = AddZoneConnectionDialog(self._presenter, element)
            window.show()
    
    def mouse_move(self, pos):
        scene = self._scene
        item = self._scene.itemAt(pos, QTransform())
        
        model = self._presenter.get_model_for_item(item)
        
        if self._highlighted_item:
            self._highlighted_item.set_highlight(False)

        if type(model) == Zone:
            self._highlighted_item = item
            self._highlighted_item.set_highlight(True)