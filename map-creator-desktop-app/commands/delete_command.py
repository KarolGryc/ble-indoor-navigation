from PySide6.QtGui import QUndoCommand
from model.node import Node
from model.wall import Wall
from model.zone import Zone

class DeleteElementsCommand(QUndoCommand):
    def __init__(self, presenter, elements: list):
        super().__init__("Delete Item")
        self._presenter = presenter
        self._elements = elements

    def undo(self):
        for element in self._elements:
            for dep in element.dependencies:
                self._presenter.model.add(dep)

            self._presenter.model.add(element)

    def redo(self):
        for element in self._elements:
            for dep in element.dependencies:
                self._presenter.model.remove(dep)
                
            self._presenter.model.remove(element)