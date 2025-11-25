from PySide6.QtWidgets import QGraphicsView

class GraphicsViewZoom:
    def __init__(self,
                 view: QGraphicsView,
                 min_zoom: float = 0.2, 
                 max_zoom: float = 10.0, 
                 scale: float = 1.1):
        self._view = view
        self._current_zoom = 1.0
        self._scale = scale
        self._min_zoom = min_zoom
        self._max_zoom = max_zoom

    def zoom_in(self):
        new_zoom = self._current_zoom * self._scale
        if new_zoom > self._max_zoom:
            return

        self._current_zoom = new_zoom
        self._view.scale(self._scale, self._scale)

    def zoom_out(self):
        new_zoom = self._current_zoom / self._scale
        if new_zoom < self._min_zoom:
            return
        
        self._current_zoom = new_zoom
        self._view.scale(1 / self._scale, 1 / self._scale)

    def reset_zoom(self):
        if self._current_zoom == 1.0:
            return
        
        scale_factor = 1.0 / self._current_zoom
        self._view.scale(scale_factor, scale_factor)
        self._current_zoom = 1.0

    @property
    def current_zoom(self) -> float:
        return self._current_zoom