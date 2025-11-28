from model.wall import Wall
from PySide6.QtWidgets import QGraphicsLineItem
from PySide6.QtGui import QPen, QColor

class WallGraphicsItem(QGraphicsLineItem):
    def __init__(self, wall: Wall):
        super().__init__(wall.start_node.x, wall.start_node.y,
                         wall.end_node.x, wall.end_node.y)
        pen = QPen(QColor("black"), 6)
        self.setPen(pen)
        self.wall = wall
        self.setFlag(QGraphicsLineItem.ItemIsSelectable, True)


        self.wall.updated.connect(self.update_item)

    def itemChange(self, change, value):
        if change == QGraphicsLineItem.ItemSceneHasChanged and value is None:
            try:
                self.wall.updated.disconnect(self.update_item)
            except TypeError:
                pass

        return super().itemChange(change, value)

    def update_item(self):
        self.update_geometry()

    def update_geometry(self):
        self.setLine(self.wall.start_node.x, self.wall.start_node.y,
                     self.wall.end_node.x, self.wall.end_node.y)