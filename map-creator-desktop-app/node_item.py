from node import Node
from PySide6.QtWidgets import QGraphicsEllipseItem, QGraphicsItem
from PySide6.QtGui import QColor

class NodeGraphicsItem(QGraphicsEllipseItem):
    def __init__(self, node: Node):
        radius = 5
        super().__init__(-radius, -radius, radius * 2, radius * 2)
        self.setBrush(QColor("blue"))
        self.node = node
        self.setPos(node.position)
