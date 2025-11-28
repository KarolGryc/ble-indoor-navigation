from PySide6.QtWidgets import QDialog, QVBoxLayout, QHBoxLayout, QLabel, QLineEdit, QComboBox, QDialogButtonBox, QMessageBox, QInputDialog
from PySide6.QtWidgets import QApplication
from PySide6.QtGui import QPalette
from model.zone import ZoneType
from model.point_of_interest import PointOfInterestType

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

def _is_dark_theme() -> bool:
    pallete = QApplication.palette()
    bg_color = pallete.color(QPalette.Window)

    return bg_color.value() < 128