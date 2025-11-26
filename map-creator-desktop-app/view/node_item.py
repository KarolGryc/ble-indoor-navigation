from model.node import Node
from PySide6.QtWidgets import QGraphicsEllipseItem
from PySide6.QtGui import QColor

class NodeGraphicsItem(QGraphicsEllipseItem):
    def __init__(self, node: Node):
        radius = 5
        super().__init__(-radius, -radius, radius * 2, radius * 2)
        self.setBrush(QColor("blue"))
        self.node = node
        self.setPos(node.position)
        self.setFlag(QGraphicsEllipseItem.ItemIsSelectable, True)

    def update_item(self):
        self.update_geometry()

    def update_geometry(self):
        self.setPos(self.node.position)
