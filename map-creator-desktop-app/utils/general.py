from PySide6.QtWidgets import QInputDialog, QMessageBox

def ask_for_zone_name(default_name="Example Zone"):
    name, ok = QInputDialog.getText(
        None, "Enter zone name", "Zone Name:", text=default_name
    )

    if not ok or name.strip() == "":
        QMessageBox.critical(None, "Invalid Name", "Zone name cannot be empty.")
        return None

    return name.strip()