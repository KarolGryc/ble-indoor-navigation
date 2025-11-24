from PySide6.QtGui import QUndoCommand
from model.node import Node
from model.wall import Wall

class DeleteElementsCommand(QUndoCommand):
    def __init__(self, presenter, elements: list):
        super().__init__("Delete Item")
        self.presenter = presenter
        self._elements = elements

    def undo(self):
        for element in self._elements:
            if isinstance(element, Node):
                self.presenter.model.add_node(element)
            elif isinstance(element, Wall):
                self.presenter.model.add_wall(element)

    def redo(self):
        for element in self._elements:
            if isinstance(element, Node):
                self.presenter.model.remove_node(element)
            elif isinstance(element, Wall):
                self.presenter.model.remove_wall(element)