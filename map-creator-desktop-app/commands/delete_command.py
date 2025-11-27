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
            if isinstance(element, Node):
                if element.owner and isinstance(element.owner, Wall):
                    self._presenter.model.add_wall(element.owner)
                    self._presenter.model.add_node(element.owner.start_node)
                    self._presenter.model.add_node(element.owner.end_node)
                elif element.owner and isinstance(element.owner, Zone):
                    # Interesing gliwtch
                    # zone = element.owner
                    # zone.corner_nodes.append(element)
                    # zone._on_node_changed()
                    # self.presenter.model.add_node(element)
                    zone = element.owner
                    self._presenter.model.add_zone(zone)
                    for node in zone.corner_nodes:
                        self._presenter.model.add_node(node)
                else:
                    self._presenter.model.add_node(element)

            elif isinstance(element, Wall):
                self._presenter.model.add_wall(element)
                self._presenter.model.add_node(element.start_node)
                self._presenter.model.add_node(element.end_node)

            elif isinstance(element, Zone):
                self._presenter.model.add_zone(element)
                for node in element.corner_nodes:
                    self._presenter.model.add_node(node)

    def redo(self):
        for element in self._elements:
            if isinstance(element, Node):
                if element.owner and isinstance(element.owner, Wall):
                    self._presenter.model.remove_wall(element.owner)
                    self._presenter.model.remove_node(element.owner.start_node)
                    self._presenter.model.remove_node(element.owner.end_node)
                elif element.owner and isinstance(element.owner, Zone):
                    # Interesing gliwtch
                    # zone = element.owner
                    # zone.corner_nodes.remove(element)
                    # zone._on_node_changed()
                    # self.presenter.model.remove_node(element)
                    zone = element.owner
                    self._presenter.model.remove_zone(zone)
                    for node in zone.corner_nodes:
                        self._presenter.model.remove_node(node)
                else:
                    self._presenter.model.remove_node(element)

            elif isinstance(element, Wall):
                self._presenter.model.remove_wall(element)
                self._presenter.model.remove_node(element.start_node)
                self._presenter.model.remove_node(element.end_node)

            elif isinstance(element, Zone):
                self._presenter.model.remove_zone(element)
                for node in element.corner_nodes:
                    self._presenter.model.remove_node(node)