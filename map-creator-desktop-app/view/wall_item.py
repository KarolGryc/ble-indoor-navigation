from model.wall import Wall
from PySide6.QtWidgets import QGraphicsLineItem
from PySide6.QtGui import QPen, QColor

class WallGraphicsItem(QGraphicsLineItem):
    def __init__(self, wall: Wall):
        super().__init__(wall.start_node.x, wall.start_node.y,
                         wall.end_node.x, wall.end_node.y)
        pen = QPen(QColor("black"), 2)
        self.setPen(pen)
        self.wall = wall
        self.setFlag(QGraphicsLineItem.ItemIsSelectable, True)

    def update_geometry(self):
        self.setLine(self.wall.start_node.x, self.wall.start_node.y,
                     self.wall.end_node.x, self.wall.end_node.y)