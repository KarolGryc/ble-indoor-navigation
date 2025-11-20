from PySide6.QtGui import QIcon

class Tool:
    name: str
    icon : QIcon = None

    def mousePress(self, event, scene): 
        pass

    def mouseMove(self, event, scene): 
        pass

    def mouseRelease(self, event, scene): 
        pass

class WallTool(Tool):
    name = "Wall"

    def mousePress(self, event, scene):
        print("WallTool: mousePress")
        # Logic to start drawing a wall
        pass

    def mouseMove(self, event, scene):
        print("WallTool: mouseMove")
        # Logic to update wall drawing as the mouse moves
        pass

    def mouseRelease(self, event, scene):
        print("WallTool: mouseRelease")
        # Logic to finalize the wall drawing
        pass

class SelectTool(Tool):
    name = "SelectTool"

    def mousePress(self, event, scene):
        print("SelectTool: mousePress")
        # Logic to start drawing a wall
        pass

    def mouseMove(self, event, scene):
        print("SelectTool: mouseMove")
        # Logic to update wall drawing as the mouse moves
        pass

    def mouseRelease(self, event, scene):
        print("SelectTool: mouseRelease")
        # Logic to finalize the wall drawing
        pass