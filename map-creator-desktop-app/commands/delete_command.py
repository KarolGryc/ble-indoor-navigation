from PySide6.QtGui import QUndoCommand

class DeleteElementsCommand(QUndoCommand):
    def __init__(self, model, elements: list):
        super().__init__("Delete Item")
        self._model = model
        self._elements = elements

    def undo(self):
        for element in self._elements:
            for dep in element.dependencies:
                self._model.add(dep)

            self._model.add(element)

    def redo(self):
        for element in self._elements:
            for dep in element.dependencies:
                self._model.remove(dep)
                
            self._model.remove(element)