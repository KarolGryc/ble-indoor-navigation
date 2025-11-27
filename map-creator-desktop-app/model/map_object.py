from PySide6.QtCore import QObject, Signal

class MapObject(QObject):
    updated = Signal()

    def should_survive_deletion_of(self, item: 'MapObject') -> bool:
        return True
    
    def getDependencies(self) -> list:
        return self.get_dependencies()

    @property
    def movables(self):
        pass