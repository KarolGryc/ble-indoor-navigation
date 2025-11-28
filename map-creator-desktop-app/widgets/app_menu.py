from PySide6.QtWidgets import QMenuBar, QMenu
from PySide6.QtGui import QAction, QKeySequence
from PySide6.QtCore import Signal

class AppMenu(QMenuBar):
    undo_triggered = Signal()
    redo_triggered = Signal()

    def __init__(self, parent=None):
        super().__init__(parent)

        self._create_file_menu()
        self._create_edit_menu()

    def _create_file_menu(self):
        file_menu = self.addMenu("File")

        exit_action = QAction("Exit", self)
        exit_action.setShortcut(QKeySequence.Quit)
        
        if self.parent():
            exit_action.triggered.connect(self.parent().close)
        file_menu.addAction(exit_action)

    def _create_edit_menu(self):
        edit_menu = self.addMenu("Edit")

        undo_action = QAction("Undo", self)
        undo_action.setShortcut(QKeySequence.Undo)
        undo_action.triggered.connect(self.undo_triggered)
        edit_menu.addAction(undo_action)

        redo_action = QAction("Redo", self)
        redo_action.setShortcut(QKeySequence.Redo)
        redo_action.triggered.connect(self.redo_triggered)
        edit_menu.addAction(redo_action)