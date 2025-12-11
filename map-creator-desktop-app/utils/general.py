from PySide6.QtGui import QPalette
from PySide6.QtWidgets import (
    QDialog, QVBoxLayout, QHBoxLayout, QLabel, QLineEdit, 
    QComboBox, QDialogButtonBox, QMessageBox, QInputDialog, 
    QFileDialog, QApplication
)

from model import ZoneType, PointOfInterestType, Building
from building_serializer import BuildingSerializer

def ask_zone_name(window_name="Zone settings",
                  default_name="Example Zone",
                  default_type=ZoneType.GENERIC) -> tuple[str, ZoneType]:
    dialog = QDialog()
    dialog.setWindowTitle(window_name)
    dialog.setMaximumWidth(300)
    dialog.setMaximumHeight(200)
    layout = QVBoxLayout(dialog)

    # Name input
    name_row = QHBoxLayout()
    name_row.addWidget(QLabel("Name:"))

    name_edit = QLineEdit(default_name)
    name_row.addWidget(name_edit)

    # Type input
    type_row = QHBoxLayout()
    type_row.addWidget(QLabel("Type:"))

    type_combo = QComboBox()    
    type_names = [e.name for e in ZoneType]
    type_combo.addItems(type_names)
    default_index = type_names.index(default_type.name)
    type_combo.setCurrentIndex(default_index)
    type_row.addWidget(type_combo)

    buttons = QDialogButtonBox(QDialogButtonBox.Ok | QDialogButtonBox.Cancel)

    layout.addLayout(name_row)
    layout.addLayout(type_row)
    layout.addWidget(buttons)

    def validate_and_accept():
        if not name_edit.text().strip():
            QMessageBox.critical(dialog, "Error", "Name cannot be empty!")
            return
        dialog.accept()
        
    buttons.accepted.connect(validate_and_accept)
    buttons.rejected.connect(dialog.reject)

    if dialog.exec() == QDialog.Accepted:
        selected_name = name_edit.text().strip()
        selected_type_str = type_combo.currentText()
        selected_type = ZoneType[selected_type_str]
        return selected_name, selected_type
    
    return None, None

def ask_poi_name_and_type(window_name="Point of Interest Details",
                          default_name="Place",
                          default_type=PointOfInterestType.GENERIC):
    dialog = QDialog()
    dialog.setWindowTitle(window_name)
    dialog.setMaximumWidth(300)
    dialog.setMaximumHeight(200)
    layout = QVBoxLayout(dialog)

    # Name input
    name_row = QHBoxLayout()
    name_row.addWidget(QLabel("Name:"))

    name_edit = QLineEdit(default_name)
    name_row.addWidget(name_edit)

    # Type input
    type_row = QHBoxLayout()
    type_row.addWidget(QLabel("Type:"))

    type_combo = QComboBox()    
    type_names = [e.name for e in PointOfInterestType]
    type_combo.addItems(type_names)
    default_index = type_names.index(default_type.name)
    type_combo.setCurrentIndex(default_index)
    type_row.addWidget(type_combo)

    buttons = QDialogButtonBox(QDialogButtonBox.Ok | QDialogButtonBox.Cancel)

    layout.addLayout(name_row)
    layout.addLayout(type_row)
    layout.addWidget(buttons)

    def validate_and_accept():
        if not name_edit.text().strip():
            QMessageBox.critical(dialog, "Error", "Name cannot be empty!")
            return
        dialog.accept()
        
    buttons.accepted.connect(validate_and_accept)
    buttons.rejected.connect(dialog.reject)

    if dialog.exec() == QDialog.Accepted:
        selected_name = name_edit.text().strip()
        selected_type_str = type_combo.currentText()
        selected_type = PointOfInterestType[selected_type_str]
        return selected_name, selected_type
    
    return None, None

def ask_floor_name(window_name="Floor Name",
                    default_name="Unnamed Floor") -> str | None:
    text, ok = QInputDialog.getText(None, window_name, "Enter new floor name:", text=default_name)
    if ok and text.strip():
        return text.strip()
    return None

def is_dark_theme() -> bool:
    pallete = QApplication.palette()
    bg_color = pallete.color(QPalette.Window)

    return bg_color.value() < 128

def load_file_dialog(parent, 
                          title="Open File", 
                          filter="JSON Files (*.json);;All Files (*)") -> str | None:
    file_dialog = QFileDialog(parent, title)
    file_dialog.setFileMode(QFileDialog.ExistingFile)
    file_dialog.setNameFilter(filter)

    if file_dialog.exec() == QFileDialog.Accepted:
        selected_files = file_dialog.selectedFiles()
        if selected_files:
            return selected_files[0]
    
    return None

def load_building(parent,
                  title="Open Building File",
                  filter="Indoor Map Files (*.inmap)") -> Building | None:
    path = load_file_dialog(parent, title, filter)
    if path is None:
        return None

    serializer = BuildingSerializer()
    building = serializer.load_from_file(path)
    if building is None:
        QMessageBox.critical(
            parent,
            "Error",
            f"Failed to load building from file: {path}"
        )
    return building

def save_file_dialog(parent,
                     title="Save File",
                     extension=".json", 
                     filter="JSON Files (*.json);;All Files (*)") -> str | None:
        file_dialog = QFileDialog(parent, title)
        file_dialog.setAcceptMode(QFileDialog.AcceptSave)
        file_dialog.setNameFilter(filter)

        if file_dialog.exec() == QFileDialog.Accepted:
            selected_files = file_dialog.selectedFiles()
            if selected_files:
                file_path = selected_files[0]
                if not file_path.lower().endswith(extension):
                    file_path += extension
                
                return file_path
        
        return None

def save_building(parent,
                  building: Building,
                  title="Save Building File",
                  extension=".inmap",
                  filter="Indoor Map Files (*.inmap)"):
        path = save_file_dialog(parent, title, extension, filter)
        if path is None:
            return None
    
        serializer = BuildingSerializer()
        success = serializer.save_to_file(building, path)
        if not success:
            QMessageBox.critical(
                parent,
                "Error",
                f"Failed to save building to file: {path}"
            )
            return None
        
        return path