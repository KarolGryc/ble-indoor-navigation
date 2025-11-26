from PySide6.QtCore import QObject, Signal

class MapObject(QObject):
    updated = Signal()

    def get_dependencies(self) -> list:
        return []
    
    def should_survive_deletion_of(self, item: 'MapObject') -> bool:
        return True
    
    @property
    def movables(self):
        pass