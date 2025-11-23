from PySide6.QtWidgets import QToolBar, QToolButton
from map_presenter import MapPresenter

from tools.tool import Tool

class Toolbar(QToolBar):
    def __init__(self, presenter: MapPresenter, tool_set: list[Tool]):
        super().__init__("Main Toolbar")
        self.presenter = presenter
        self.presenter.current_tool_changed.connect(self._on_current_tool_changed)
        
        self._undo_button = QToolButton()
        self._undo_button.setText("Undo")
        self._undo_button.clicked.connect(self.presenter._undo_stack.undo)
        self.addWidget(self._undo_button)

        self._redo_button = QToolButton()
        self._redo_button.setText("Redo")
        self._redo_button.clicked.connect(self.presenter._undo_stack.redo)
        self.addWidget(self._redo_button)

        for tool in tool_set:
            button = QToolButton()
            self.addWidget(button)
            button.setText(type(tool).__name__)
            button.clicked.connect(lambda checked, t=tool: self._set_current_tool(t))

    def _set_current_tool(self, tool):
        self.presenter.current_tool = tool

    def _on_current_tool_changed(self, tool):
        # Handle changing style of buttons based on current tool
        pass
    