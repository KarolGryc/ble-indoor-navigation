from PySide6.QtWidgets import QGraphicsScene
from PySide6.QtGui import QTransform
from tools.tool import Tool
from map_presenter import MapPresenter
from commands.element_move_command import MoveElementsCommand

class SelectTool(Tool):
    def __init__(self, presenter: MapPresenter, scene: QGraphicsScene, name="Select Tool"):
        super().__init__(presenter, scene, name)

        self._is_dragging = False
        self._drag_start_pos = None
        self._last_drag_pos = None

        self._moving_model_objects = []

    def deactivate(self):
        pass

    def mouse_click(self, pos):
        item = self.scene.itemAt(pos, QTransform())
        print(f"SelectTool: Clicked at {pos}, item: {item}")

        if item is None:
            return

        if not item.isSelected():
            item.setSelected(True)

        self._is_dragging = True
        self._drag_start_pos = pos
        self._last_drag_pos = pos

        self._moving_model_objects = []
        print("Selected items:", self.scene.selectedItems())
        for selected_item in self.scene.selectedItems():
            model_object = self.presenter.get_model_for_item(selected_item)
            print("Model_object:", model_object)
            if model_object:
                self._moving_model_objects.append(model_object)

    def mouse_move(self, pos):
        if self._is_dragging:
            delta = pos - self._last_drag_pos
            self._last_drag_pos = pos

            for item in self.scene.selectedItems():
                item.moveBy(delta.x(), delta.y())

    def mouse_release(self, pos):
        print(f"SelectTool: Mouse released at {pos}")
        if self._is_dragging:
            total_delta = pos - self._drag_start_pos

            if total_delta.manhattanLength() > 0 and self._moving_model_objects:
                back_vector = self._drag_start_pos - pos                
                for item in self.scene.selectedItems():
                    item.moveBy(-back_vector.x(), back_vector.y())

                move_command = MoveElementsCommand(
                    self.presenter.model,
                    self._moving_model_objects,
                    total_delta
                )
                self.presenter.execute_command(move_command)

            self._is_dragging = False
            self._drag_start_pos = None
            self._last_drag_pos = None