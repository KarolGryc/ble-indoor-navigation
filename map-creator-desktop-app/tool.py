from abc import ABC, abstractmethod

class Tool(ABC):
    @abstractmethod
    def mouse_click(self, pos):
        pass

    @abstractmethod
    def mouse_move(self, pos):
        pass