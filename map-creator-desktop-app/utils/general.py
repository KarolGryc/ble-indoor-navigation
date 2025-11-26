from PySide6.QtWidgets import QDialog, QVBoxLayout, QHBoxLayout, QLabel, QLineEdit, QComboBox, QDialogButtonBox, QMessageBox, QInputDialog
from model.point_of_interest import PointOfInterestType

def ask_zone_name(default_name="Example Zone"):
    name, ok = QInputDialog.getText(
        None, "Enter zone name", "Zone Name:", text=default_name
    )

    if not ok or name.strip() == "":
        QMessageBox.critical(
            None, "Invalid Name", "Zone name cannot be empty."
        )
        return None

    return name.strip()

def ask_poi_name_and_type(parent=None,
                          window_name="Point of Interest Details",
                          default_name="Place",
                          default_type=PointOfInterestType.GENERIC):
    dialog = QDialog(parent)
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