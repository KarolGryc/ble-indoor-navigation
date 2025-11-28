import sys
from PySide6.QtWidgets import QApplication
from map_creator_app import MapCreatorApp

def main():
    app = QApplication(sys.argv)
    window = MapCreatorApp()
    window.show()
    sys.exit(app.exec())

if __name__ == "__main__":
    main()