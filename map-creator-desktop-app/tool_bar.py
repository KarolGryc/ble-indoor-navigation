from PySide6.QtWidgets import QToolButton, QToolBar, QButtonGroup, QWidget, QVBoxLayout

class ToolBar(QToolBar):
    def __init__(self, viewmodel):
        super().__init__("Tools")
        self._app_viewmodel = viewmodel
        self._button_group = QButtonGroup(self, exclusive=True)
        self._buttons = {}

        for tool in self._app_viewmodel.tools:
            btn = self._create_tool_button(tool)
            self.addWidget(btn)
            self._button_group.addButton(btn)
            self._buttons[type(tool)] = btn

            if tool is self._app_viewmodel.active_tool:
                btn.setChecked(True)

            btn.toggled.connect(lambda checked, t=tool: checked and setattr(self._app_viewmodel, "active_tool", t))

        self._app_viewmodel.activeToolChanged.connect(self.on_tool_changed)

    def _create_tool_button(self, tool):
        btn = QToolButton()
        btn.setCheckable(True)

        if tool.name:
            btn.setText(tool.name)
            btn.setToolTip(tool.name)
        
        if tool.icon:
            btn.setIcon(tool.icon)
        
        return btn

    def on_tool_changed(self, tool):
        if btn := self._buttons.get(type(tool)):
            btn.setChecked(True)