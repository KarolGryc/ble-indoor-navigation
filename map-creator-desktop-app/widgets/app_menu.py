from PySide6.QtGui import QAction, QKeySequence
from PySide6.QtCore import Signal
from PySide6.QtWidgets import QMenuBar, QMenu

from .map_view import MapTheme

class AppMenu(QMenuBar):
    undo_triggered = Signal()
    redo_triggered = Signal()
    map_theme_triggered = Signal(MapTheme)
    save_requested = Signal()

    def __init__(self, parent=None):
        super().__init__(parent)

        self._create_file_menu()
        self._create_edit_menu()
        self._create_view_menu()

    def _create_file_menu(self):
        file_menu = self.addMenu("File")

        save_action = QAction("Save", self)
        save_action.setShortcut(QKeySequence.Save)
        save_action.triggered.connect(self.save_requested)
        file_menu.addAction(save_action)

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

    def _create_view_menu(self):
        view_menu = self.addMenu("View")

        theme_menu = QMenu("Map Theme", self)
        system_theme_action = QAction("System", self)
        light_theme_action = QAction("Light", self)
        dark_theme_action = QAction("Dark", self)
        system_theme_action.triggered.connect(lambda: self.map_theme_triggered.emit(MapTheme.SYSTEM))
        light_theme_action.triggered.connect(lambda: self.map_theme_triggered.emit(MapTheme.LIGHT))
        dark_theme_action.triggered.connect(lambda: self.map_theme_triggered.emit(MapTheme.DARK))
        theme_menu.addAction(system_theme_action)
        theme_menu.addAction(light_theme_action)
        theme_menu.addAction(dark_theme_action)
        view_menu.addMenu(theme_menu)